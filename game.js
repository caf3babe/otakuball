// =============================================================
// OTAKUBALL – Web Port  (Matter.js physics, Canvas rendering)
// =============================================================
(function () {
'use strict';

const GIT_HASH = '__GIT_HASH__';

const { Engine, Bodies, Body, World, Events, Constraint, Vector, Composite, Runner } = Matter;

// ── Canvas / display ─────────────────────────────────────────
const canvas = document.getElementById('gameCanvas');
const ctx    = canvas.getContext('2d');
const GW = 1920, GH = 1080;   // logical game size
let   displayScale = 1;

canvas.width  = GW;
canvas.height = GH;

function resizeCanvas() {
  const s = Math.min(window.innerWidth / GW, window.innerHeight / GH);
  displayScale = s;
  canvas.style.width  = (GW * s) + 'px';
  canvas.style.height = (GH * s) + 'px';
}
window.addEventListener('resize', resizeCanvas);
resizeCanvas();

// ── Physics scale ─────────────────────────────────────────────
// Original dyn4j used scale = 128 (px per metre).
// Here canvas coords ARE the pixel coords, so:
//   canvas_x = GW/2 + dyn4j_x * SCALE
//   canvas_y = GH/2 - dyn4j_y * SCALE
const SCALE = 128;
function d2c(dx, dy) { return { x: GW/2 + dx*SCALE, y: GH/2 - dy*SCALE }; }

// ── Game state ────────────────────────────────────────────────
const S = { MENU:'menu', PLAYING:'playing', PAUSED:'paused', COMPLETE:'complete' };
let gameState  = S.MENU;
let curLevel   = 0;   // 0-indexed (6 levels total)
let shots      = 0;   // shots used this level
let totalScore = 0;
let playerName = '';
let goalScored = false;
let levelFailed = false;
let failTimer   = 0;
let resetTimer  = 0;   // for auto-reset when ball is still after 3 shots
let resetArmed  = false;

// Level 4 (zero-g) timeout reset
let zeroGResetTimer = 0;

// Level 6 goku message
let gokuHits = 0;

// Explosion / goal animation
let showExplosion  = false;
let explosionTimer = 0;
let explosionX = 0, explosionY = 0;
const EXPLOSION_DURATION = 2.5; // seconds

// ── Assets ───────────────────────────────────────────────────
const images = {};
const sounds  = {};

const IMAGE_FILES = {
  menuBg:      'background_menu_new.png',
  ball:        'SoccerBall.png',
  dragonball:  'dragonball.png',
  barrel:      'fass2d.png',
  blade:       'blade.png',
  brick:       'brick.png',
  fixBrick:    'fixBrick.png',
  lvl2Goal:    'lvl2_goal.png',
  tunnel:      'tunnel.png',
  bg1:         'Background.png',
  lvl2Bg:      'lvl2_background.png',
  salah:       'salah.png',
  solanke:     'solanke.png',
  lallana:     'lallana.png',
  mane:        'mane.png',
  firmino:     'firmino.png',
  alisson:     'alisson.png',
  camera:      'camera_transparent.png',
  stadium:     'trib\u00fcne_level3goal.png',
  level4:      'Level4.png',
  asteroid:    'asteroid.png',
  lvl5Bg:      'lvl5_background.png',
  kamehame:    'kamehame.jpg',
  goku:        'goku.png',
  explosion:   'explosion.png',
  goalPopup:   'goal_popup.png',
  backBtn:     'back_transparent.png',
};

const SOUND_FILES = {
  theme1:     'theme_lvl1.wav',
  theme2:     'lvl2_theme.wav',
  theme3:     'sega_lvl3.wav',
  theme5:     'asteroids.wav',
  theme6:     'chala.wav',
  goal1:      'explosion2.wav',
  goal2:      'stage2_clear.wav',
  goal3:      'gol_lvl3.wav',
  goal5:      'wormhole.wav',
  kick:       'ball_kick.wav',
  bounce1:    'first_ball_bounce.wav',
  bounce2:    'second_ball_bounce.wav',
  breakbrick: 'breakbrick.wav',
  died:       'died.wav',
};

function loadAssets() {
  return Promise.all(
    Object.entries(IMAGE_FILES).map(([k, f]) => new Promise(res => {
      const img = new Image();
      img.onload  = () => { images[k] = img; res(); };
      img.onerror = () => res();
      img.src = 'src/images/' + f;
    }))
  ).then(() => {
    Object.entries(SOUND_FILES).forEach(([k, f]) => {
      const a = new Audio('src/sounds/' + f);
      a.preload = 'auto';
      sounds[k] = a;
    });
  });
}

let currentThemeKey = null;
function playTheme(key) {
  stopAllSounds();
  if (!key || !sounds[key]) return;
  currentThemeKey = key;
  sounds[key].loop = true;
  sounds[key].currentTime = 0;
  sounds[key].play().catch(() => {});
}
function playSfx(key) {
  const s = sounds[key]; if (!s) return;
  try { const c = s.cloneNode(); c.play().catch(()=>{}); } catch(e) {}
}
function stopAllSounds() {
  Object.values(sounds).forEach(s => { try { s.pause(); s.currentTime = 0; } catch(e){} });
  currentThemeKey = null;
}

// ── Highscores ────────────────────────────────────────────────
let highscores = [];
function loadHighscores() {
  try { highscores = JSON.parse(localStorage.getItem('otakuball_hs') || '[]'); } catch(e) { highscores = []; }
}
function saveHighscore(name, score) {
  // Update or add
  const existing = highscores.find(h => h.name === name);
  if (existing) { if (score > existing.score) existing.score = score; }
  else highscores.push({ name, score });
  highscores.sort((a,b) => b.score - a.score);
  highscores = highscores.slice(0, 7);
  localStorage.setItem('otakuball_hs', JSON.stringify(highscores));
}

// ── Physics engine ────────────────────────────────────────────
let engine, world;
let ball;
let levelBodies    = [];  // { body, imageKey, color, type }
let brickBodies    = [];
let blades         = [];
let gokuBody       = null;
let asteroidBodies = [];
let goalSensor     = null;
let groundBody     = null;

const LABELS = { BALL:'ball', GOAL:'goal', BLADE:'blade', BRICK:'brick', GROUND:'ground', GOKU:'goku', BORDER:'border' };
const MAX_SHOTS = 3;

function initEngine(gravY = 0.5) {
  if (engine) { Events.off(engine); Engine.clear(engine); }
  engine = Engine.create();
  world  = engine.world;
  engine.gravity.x = 0;
  engine.gravity.y = gravY;
}

function addBorders() {
  const T = 60;
  const borders = [
    Bodies.rectangle(GW/2,    -T/2,    GW+T*2, T,   { isStatic:true, label:LABELS.BORDER, restitution:0.3, friction:0.5 }),
    Bodies.rectangle(GW/2,  GH+T/2,    GW+T*2, T,   { isStatic:true, label:LABELS.BORDER, restitution:0.3, friction:0.5 }),
    Bodies.rectangle(-T/2,   GH/2,     T,   GH+T*2, { isStatic:true, label:LABELS.BORDER, restitution:0.3, friction:0.5 }),
    Bodies.rectangle(GW+T/2, GH/2,     T,   GH+T*2, { isStatic:true, label:LABELS.BORDER, restitution:0.3, friction:0.5 }),
  ];
  World.add(world, borders);
}

function mkStatic(dx, dy, dw, dh, opts={}) {
  const c = d2c(dx, dy);
  return Bodies.rectangle(c.x, c.y, dw*SCALE, dh*SCALE,
    { isStatic:true, restitution:0.3, friction:0.5, ...opts });
}
function mkCircle(dx, dy, dr, opts={}) {
  const c = d2c(dx, dy);
  return Bodies.circle(c.x, c.y, dr*SCALE, { restitution:0.5, ...opts });
}

// Make the ball body
function makeBall(dx, dy, imageKey) {
  const c = d2c(dx, dy);
  ball = Bodies.circle(c.x, c.y, 0.14*SCALE, {
    restitution: 0.5, friction: 0.5, frictionAir: 0.016,
    density: 0.002, label: LABELS.BALL,
  });
  World.add(world, ball);
  levelBodies.push({ body:ball, imageKey, type:'ball' });
}

// ── Level reset helpers ───────────────────────────────────────
function clearLevel() {
  if (world) World.clear(world, false);
  levelBodies    = [];
  brickBodies    = [];
  blades         = [];
  gokuBody       = null;
  asteroidBodies = [];
  goalSensor     = null;
  groundBody     = null;
  goalScored     = false;
  levelFailed    = false;
  shots          = 0;
  showExplosion  = false;
  explosionTimer = 0;
  resetArmed     = false;
  resetTimer     = 0;
  zeroGResetTimer = 0;
  gokuHits       = 0;
}

function restartLevel() { clearLevel(); initLevel(curLevel); }
function nextLevel()    { curLevel++; clearLevel(); initLevel(curLevel); }

// ── Collision handling ────────────────────────────────────────
function handlePair(A, B) {
  const labels = [A.label, B.label];

  // Goal scored
  if (labels.includes(LABELS.BALL) && labels.includes(LABELS.GOAL)) {
    onGoalScored();
  }
  // Blade kills (level 2)
  if (curLevel === 1 && labels.includes(LABELS.BALL) && labels.includes(LABELS.BLADE)) {
    onLevelFailed();
  }
  // Brick destroyed
  if (labels.includes(LABELS.BALL) && labels.includes(LABELS.BRICK)) {
    const brick = A.label === LABELS.BRICK ? A : B;
    World.remove(world, brick);
    brickBodies = brickBodies.filter(b => b !== brick);
    levelBodies = levelBodies.filter(o => o.body !== brick);
    playSfx('breakbrick');
  }
  // Goku hit counter (level 6)
  if (curLevel === 5 && labels.includes(LABELS.BALL) && labels.includes(LABELS.GOKU)) {
    gokuHits++;
  }
}

function setupCollisions() {
  Events.on(engine, 'collisionStart', (ev) => {
    if (goalScored || levelFailed) return;
    ev.pairs.forEach(pair => handlePair(pair.bodyA, pair.bodyB));
  });
  // collisionActive catches any frame the ball overlaps the goal sensor (belt-and-suspenders)
  Events.on(engine, 'collisionActive', (ev) => {
    if (goalScored || levelFailed) return;
    ev.pairs.forEach(pair => {
      const labels = [pair.bodyA.label, pair.bodyB.label];
      if (labels.includes(LABELS.BALL) && labels.includes(LABELS.GOAL)) {
        onGoalScored();
      }
    });
  });
}

function onGoalScored() {
  if (goalScored) return;
  goalScored = true;
  showExplosion  = true;
  explosionTimer = 0;
  explosionX = ball.position.x;
  explosionY = ball.position.y;

  const remaining = Math.max(0, MAX_SHOTS - shots);
  totalScore += remaining * 10;

  stopAllSounds();
  const sfxKeys = ['goal1','goal2','goal3','goal1','goal5','goal1'];
  playSfx(sfxKeys[curLevel] || 'goal1');
}

function onLevelFailed() {
  if (levelFailed) return;
  levelFailed = true;
  failTimer = 0;
  stopAllSounds();
  playSfx('died');
}

// ── Level initialisers ────────────────────────────────────────
function initLevel(n) {
  const inits = [initL1, initL2, initL3, initL4, initL5, initL6];
  if (inits[n]) inits[n]();
}

// ── Level 1: Soccer / Barrels ─────────────────────────────────
function initL1() {
  initEngine(0.5);
  addBorders();
  setupCollisions();

  // Background
  levelBodies.push({ type:'bg', imageKey:'bg1' });

  // Invisible ground body (for reset detection)
  const gy = -4.21875 + 0.23;
  groundBody = mkStatic(0, gy, GW/SCALE, 0.08, { label:LABELS.GROUND, collisionFilter:{ mask:0xFFFFFFFF } });
  groundBody.render = { visible:false };
  World.add(world, groundBody);

  // Goal crossbar (top) and post (left side opening)
  const crossbar = mkStatic(7.0, gy + 1.5 + 0.05 + 0.08/2, 1, 0.1, { label:'goalPart' });
  const goalPost = mkStatic(7.5 - 1 + 0.05, gy + 0.75 + 0.08/2, 0.1, 1.5, { label:'goalPart' });
  levelBodies.push({ body:crossbar, color:'#ffffff' });
  levelBodies.push({ body:goalPost, color:'#ffffff' });
  World.add(world, [crossbar, goalPost]);

  // Goal sensor
  goalSensor = mkStatic(7.0, gy + 0.75 + 0.08/2, 1.0, 1.5,
    { label:LABELS.GOAL, isSensor:true, collisionFilter:{ category:0x0002, mask:0x0001 } });
  World.add(world, goalSensor);

  // Barrels (dynamic)
  const bw = 500/SCALE/10, bh = 589/SCALE/10;
  for (let i = 1; i <= 4; i++) {
    const c = d2c(2, gy + bh*i + 0.08/2);
    const barrel = Bodies.rectangle(c.x, c.y, bw*SCALE, bh*SCALE, {
      restitution:0.5, friction:0.2, density:0.001, label:'barrel'
    });
    World.add(world, barrel);
    levelBodies.push({ body:barrel, imageKey:'barrel' });
  }

  // Ball
  makeBall(-6.5, 0, 'ball');
  ball.collisionFilter = { category:0x0001, mask:0xFFFFFFFF };

  playTheme('theme1');
}

// ── Level 2: Saw blades / Bricks ──────────────────────────────
function initL2() {
  initEngine(0.5);
  addBorders();
  setupCollisions();

  levelBodies.push({ type:'bg', imageKey:'lvl2Bg' });

  const gy = -4.21875 + 0.7;
  groundBody = mkStatic(0, gy, GW/SCALE, 0.08, { label:LABELS.GROUND });
  World.add(world, groundBody);

  const bW = 82/SCALE/1.5, bH = 73/SCALE/1.5;
  const fx = -5;

  // Fixed bricks (indestructible)
  const fixedDefs = [
    { x: fx + bW*22, y: -2 },
    { x: fx + bW*11, y: bH - 1.5 },
  ];
  fixedDefs.forEach(p => {
    const b = mkStatic(p.x, p.y, bW, bH, { label:'fixedBrick' });
    World.add(world, b);
    levelBodies.push({ body:b, imageKey:'fixBrick' });
  });

  // Breakable bricks
  const brickDefs = [
    { x: fx,          y: -1.5 }, { x: fx+bW,     y: -1.5 },
    { x: fx+bW*2,     y: -1.5 }, { x: fx+bW*3,   y: -1.5 },
    { x: fx+bW*6,     y: bH-1.5 }, { x: fx+bW*7, y: bH-1.5 },
    { x: fx+bW*8,     y: bH-1.5 }, { x: fx+bW*9, y: bH-1.5 },
    { x: fx+bW*10,    y: bH-1.5 }, { x: fx+bW*12, y: bH-1.5 },
    { x: fx+bW*21,    y: -2 },   { x: fx+bW*23,   y: -2 },
  ];
  brickDefs.forEach(p => {
    const b = mkStatic(p.x, p.y, bW, bH, { label:LABELS.BRICK });
    World.add(world, b);
    brickBodies.push(b);
    levelBodies.push({ body:b, imageKey:'brick' });
  });

  // Saw blades (pendulums)
  const anchorL = d2c(fx + bW*11, bH - 1.5);
  const anchorR = d2c(fx + bW*22, -2);
  const bladeL  = mkCircle(fx + bW*11, bH - 1.5 - 1.7, 0.40, { label:LABELS.BLADE, density:0.5, frictionAir:0 });
  const bladeR  = mkCircle(fx + bW*22, -2 - 1.2,       0.40, { label:LABELS.BLADE, density:0.5, frictionAir:0 });
  Body.setVelocity(bladeL, { x:-4, y:0 });
  Body.setVelocity(bladeR, { x: 4, y:0 });
  World.add(world, [bladeL, bladeR]);
  blades = [bladeL, bladeR];
  levelBodies.push({ body:bladeL, imageKey:'blade', type:'blade' });
  levelBodies.push({ body:bladeR, imageKey:'blade', type:'blade' });

  // Rope constraints
  [{ anchor:anchorL, blade:bladeL, len:1.7 }, { anchor:anchorR, blade:bladeR, len:1.2 }].forEach(c => {
    World.add(world, Constraint.create({
      pointA: c.anchor, bodyB: c.blade, length: c.len*SCALE, stiffness:0.02, damping:0.1
    }));
  });

  // Goal appearance + sensor
  const goalAppC = d2c(7.5-0.5, gy + 1 + 0.45 + 0.08/2);
  const goalApp  = Bodies.rectangle(goalAppC.x, goalAppC.y, 1*SCALE, 1.5*SCALE, { isStatic:true, label:'goalApp' });
  World.add(world, goalApp);
  levelBodies.push({ body:goalApp, imageKey:'lvl2Goal' });

  goalSensor = mkStatic(7.5-0.70, gy + 0.60 + 0.08/2, 1.0, 1.2, { label:LABELS.GOAL, isSensor:true });
  World.add(world, goalSensor);

  // Tunnel decoration
  const tunnelC = d2c(2, gy + 98/2/SCALE + 0.08/2);
  const tunnel  = Bodies.rectangle(tunnelC.x, tunnelC.y, 97/SCALE*SCALE, 98/SCALE*SCALE, { isStatic:true, label:'tunnel' });
  World.add(world, tunnel);
  levelBodies.push({ body:tunnel, imageKey:'tunnel' });

  World.add(world, groundBody);

  // Ball (starts 2 units lower)
  makeBall(-6.5, -2, 'ball');

  playTheme('theme2');
}

// ── Level 3: Stadium / Players ────────────────────────────────
function initL3() {
  initEngine(0.5);
  addBorders();
  setupCollisions();

  levelBodies.push({ type:'bg', imageKey:'stadium' });

  const gy = -4.21875 + 0.05;
  groundBody = mkStatic(0, gy, GW/SCALE, 16/SCALE, { label:LABELS.GROUND });
  World.add(world, groundBody);

  // Static obstacles
  const statics = [
    { dx:7.5-6.3,  dy:4.21875-2.0,   w:0.7, h:0.7,  img:'camera'  },
    { dx:7.5-11.15,dy:4.21875-7.5,   w:0.8, h:1.8,  img:'firmino' },
    { dx:7.5-7.0,  dy:4.21875-7.5,   w:0.9, h:1.8,  img:'mane'    },
    { dx:7.5-3.6,  dy:4.21875-7.5,   w:1.2, h:1.8,  img:'alisson' },
  ];
  statics.forEach(s => {
    const b = mkStatic(s.dx, s.dy, s.w, s.h, { label:'obstacle' });
    World.add(world, b);
    levelBodies.push({ body:b, imageKey:s.img });
  });

  // Bouncy player obstacles (dynamic, high restitution)
  const bouncyPlayers = [
    { dx:-2.85, dy:-1.5, w:0.8, h:1.8, img:'salah'   },
    { dx:-1.85, dy:-0.8, w:1.2, h:1.8, img:'solanke' },
    { dx:-0.70, dy:-1.5, w:1.1, h:1.8, img:'lallana' },
  ];
  bouncyPlayers.forEach(p => {
    const c = d2c(p.dx, p.dy);
    const b = Bodies.rectangle(c.x, c.y, p.w*SCALE, p.h*SCALE, {
      isStatic:false, restitution:0.965, friction:2,
      density:0.05, frictionAir:0.15, label:'playerMid'
    });
    World.add(world, b);
    levelBodies.push({ body:b, imageKey:p.img });
  });

  // Goal sensor (thin vertical strip far right)
  goalSensor = mkStatic(7.5-0.65, gy + 1 + 0.05, 1.0, 1.3, { label:LABELS.GOAL, isSensor:true });
  World.add(world, goalSensor);

  makeBall(-6.5, 0, 'ball');

  playTheme('theme3');
}

// ── Level 4: Zero-gravity maze ────────────────────────────────
function initL4() {
  initEngine(0);   // zero gravity
  addBorders();
  setupCollisions();

  levelBodies.push({ type:'bg', imageKey:'level4' });

  // Ball new start: (4.5, -1.5)
  makeBall(4.5, -1.5, 'ball');

  // Goal sensor (upper-right area)
  const gp = d2c(6.3, 2.28);
  goalSensor = Bodies.rectangle(gp.x, gp.y, 0.5*SCALE, 1.3*SCALE,
    { isStatic:true, isSensor:true, label:LABELS.GOAL });
  World.add(world, goalSensor);

  // Invisible maze walls derived from Java code positions
  const walls = [
    { dx:-1.62, dy:-0.92, dw:5, dh:0.125, angle:0 },
    { dx: 4.18, dy: 2.58, dw:5, dh:0.125, angle:0 },
    { dx: 4.78, dy: 0.28, dw:0.2, dh:3,  angle:0 },
    { dx: 5.78, dy: 1.78, dw:2,  dh:0.125, angle:0 },
    { dx: 3.48, dy: 3.28, dw:0.2, dh:3,  angle:0 },
    // Rotated walls  (angle in radians = original rotate arg mod 2π)
    { dx: 2.58, dy:-2.42, dw:5, dh:0.125, angle: 6.85 % (2*Math.PI) },
    { dx: 1.98, dy:-1.12, dw:4, dh:0.125, angle:(7.5-0.45) % (2*Math.PI) },
  ];
  walls.forEach(w => {
    const c = d2c(w.dx, w.dy);
    const b = Bodies.rectangle(c.x, c.y, w.dw*SCALE, w.dh*SCALE,
      { isStatic:true, angle:w.angle, label:'mazeWall', restitution:0.8, friction:0.05 });
    World.add(world, b);
    levelBodies.push({ body:b, color:'rgba(0,0,0,0)', type:'invisible' });
  });
}

// ── Level 5: Space / Asteroids ────────────────────────────────
function initL5() {
  initEngine(0.05);
  addBorders();
  setupCollisions();

  levelBodies.push({ type:'bg', imageKey:'lvl5Bg' });

  const gy = -4.21875 + 0.3;
  groundBody = mkStatic(0, gy, GW/SCALE, 0.08, { label:LABELS.GROUND });
  World.add(world, groundBody);

  // Wormhole goal  (tor ellipse → circle approx)
  const torC = d2c(7.1, -2.62);
  goalSensor = Bodies.circle(torC.x, torC.y, 70/2/SCALE*SCALE,
    { isStatic:true, isSensor:true, label:LABELS.GOAL });
  World.add(world, goalSensor);
  levelBodies.push({ body:goalSensor, color:'rgba(100,0,255,0.25)', type:'goal' });

  // Asteroids
  const asteroidDefs = [
    { dx:0,          dy:0, vx:5,  vy:-4 },
    { dx:-600/SCALE, dy:0, vx:5,  vy:-8 },
    { dx:450/SCALE,  dy:0, vx:4,  vy:0  },
  ];
  asteroidDefs.forEach(a => {
    const c = d2c(a.dx, a.dy);
    const ast = Bodies.circle(c.x, c.y, 100/SCALE*SCALE, {
      restitution:1, friction:0, frictionAir:0, density:0.005, label:'asteroid'
    });
    Body.setVelocity(ast, { x:a.vx, y:a.vy });
    World.add(world, ast);
    asteroidBodies.push(ast);
    levelBodies.push({ body:ast, imageKey:'asteroid' });
  });

  makeBall(-6.5, 0, 'ball');

  playTheme('theme5');
}

// ── Level 6: Dragon Ball / Goku ───────────────────────────────
function initL6() {
  initEngine(0.5);
  addBorders();
  setupCollisions();

  levelBodies.push({ type:'bg', imageKey:'kamehame' });

  const gy = -4.21875 + 0.3;
  groundBody = mkStatic(0, gy, GW/SCALE, 0.08, { label:LABELS.GROUND });
  World.add(world, groundBody);

  // Goku (tracks ball Y)
  const gokuC = d2c(450/SCALE, 0);
  gokuBody = Bodies.circle(gokuC.x, gokuC.y, 100/SCALE*SCALE, {
    isStatic:false, restitution:1, friction:0, frictionAir:0, density:0.005, label:LABELS.GOKU
  });
  Body.setVelocity(gokuBody, { x:3, y:0 });
  World.add(world, gokuBody);
  levelBodies.push({ body:gokuBody, imageKey:'goku' });

  // No goal sensor in level 6
  goalSensor = null;

  makeBall(-6.5, 0, 'dragonball');

  playTheme('theme6');
}

// ── Input handling ────────────────────────────────────────────
let pressPoint = null, dragPoint = null;

function canvasToGame(e) {
  const rect = canvas.getBoundingClientRect();
  const touch = (e.touches && e.touches[0]) || (e.changedTouches && e.changedTouches[0]);
  const clientX = touch ? touch.clientX : e.clientX;
  const clientY = touch ? touch.clientY : e.clientY;
  return {
    x: (clientX - rect.left) / displayScale,
    y: (clientY - rect.top)  / displayScale,
  };
}

canvas.addEventListener('mousedown',  onPointerDown);
canvas.addEventListener('mousemove',  onPointerMove);
canvas.addEventListener('mouseup',    onPointerUp);
canvas.addEventListener('touchstart', e => { e.preventDefault(); onPointerDown(e); }, { passive:false });
canvas.addEventListener('touchmove',  e => { e.preventDefault(); onPointerMove(e); }, { passive:false });
canvas.addEventListener('touchend',   e => { e.preventDefault(); onPointerUp(e);   }, { passive:false });

function onPointerDown(e) {
  if (gameState !== S.PLAYING) return;
  if (goalScored || levelFailed) return;
  if (shots >= MAX_SHOTS) return;
  pressPoint = canvasToGame(e);
  dragPoint  = null;
}
function onPointerMove(e) {
  if (!pressPoint) return;
  dragPoint = canvasToGame(e);
}
function onPointerUp(e) {
  if (!pressPoint) return;
  const rel = canvasToGame(e);

  // Check back button click (top-right area)
  if (isBackButtonClick(rel)) {
    pressPoint = null; dragPoint = null;
    pauseGame();
    return;
  }

  if (gameState === S.PLAYING && !goalScored && !levelFailed && shots < MAX_SHOTS) {
    // Velocity calc mirrors Java: (xPress-xRelease)/scale*8
    let vx = (pressPoint.x - rel.x) / SCALE * 8;  // dyn4j units/s
    let vy = (pressPoint.y - rel.y) / SCALE * -8;  // invert Y (dyn4j Y up)

    if (vx > 0 && vy > 0) {
      // Cap at 15 dyn4j units/s
      vx = Math.min(vx, 15);
      vy = Math.min(vy, 15);
      // Convert to Matter.js px/step (60fps, SCALE px/unit)
      const mvx = vx * SCALE / 60;
      const mvy = -vy * SCALE / 60;  // flip Y for canvas
      Body.setVelocity(ball, { x:mvx, y:mvy });
      shots++;
      resetArmed = true;
      resetTimer = 0;
      playSfx('kick');
    }
  }

  pressPoint = null;
  dragPoint  = null;
}

// Back button occupies top-right corner in game coords
const BACK_BTN = { x: GW - 270, y: 10, w: 260, h: 70 };
function isBackButtonClick(p) {
  return p.x > BACK_BTN.x && p.x < BACK_BTN.x + BACK_BTN.w &&
         p.y > BACK_BTN.y && p.y < BACK_BTN.y + BACK_BTN.h;
}

// ── Pause / resume ────────────────────────────────────────────
function pauseGame() {
  gameState = S.PAUSED;
  stopAllSounds();
  document.getElementById('menuOverlay').classList.remove('hidden');
  document.getElementById('btnResume').style.display = 'block';
}
function resumeGame() {
  gameState = S.PLAYING;
  document.getElementById('menuOverlay').classList.add('hidden');
  if (currentThemeKey) {
    const themeKeys = ['theme1','theme2','theme3',null,'theme5','theme6'];
    playTheme(themeKeys[curLevel]);
  }
}

// ── Update loop ───────────────────────────────────────────────
let lastTime = 0;
function update(ts) {
  const dt = Math.min((ts - lastTime) / 1000, 0.05); // cap at 50ms
  lastTime = ts;

  if (gameState === S.PLAYING) {
    Engine.update(engine, dt * 1000);

    // Position-based goal detection (handles ball tunneling through thin sensors)
    if (goalSensor && ball && !goalScored && !levelFailed) {
      const bx = ball.position.x;
      const by = ball.position.y;
      const br = ball.circleRadius;
      if (goalSensor.circleRadius) {
        // Circle sensor (Level 5 wormhole)
        const dx = bx - goalSensor.position.x;
        const dy = by - goalSensor.position.y;
        if (Math.sqrt(dx*dx + dy*dy) < goalSensor.circleRadius + br) onGoalScored();
      } else {
        // Rectangle sensor
        const gs = goalSensor.bounds;
        if (bx + br > gs.min.x && bx - br < gs.max.x &&
            by + br > gs.min.y && by - br < gs.max.y) onGoalScored();
      }
    }
    // Level 6: hitting Goku scores goal
    if (curLevel === 5 && gokuHits >= 1 && !goalScored && !levelFailed) onGoalScored();

    // Level 6: Goku tracks ball Y
    if (curLevel === 5 && gokuBody && ball) {
      Body.setPosition(gokuBody, { x:gokuBody.position.x, y:ball.position.y });
      // Bounce goku horizontally off walls
      if (gokuBody.position.x < 100 || gokuBody.position.x > GW - 100) {
        Body.setVelocity(gokuBody, { x:-gokuBody.velocity.x, y:0 });
      }
    }

    // Explosion animation → next level
    if (showExplosion) {
      explosionTimer += dt;
      if (explosionTimer >= EXPLOSION_DURATION) {
        showExplosion = false;
        if (curLevel >= 5) {
          saveHighscore(playerName, totalScore);
          gameState = S.COMPLETE;
          showCompleteScreen();
        } else {
          nextLevel();
        }
      }
    }

    // Level failed → restart
    if (levelFailed) {
      failTimer += dt;
      if (failTimer > 2.0) restartLevel();
    }

    // Auto-reset after 3 shots when ball stops (all levels except 4)
    if (curLevel !== 3 && resetArmed && shots >= MAX_SHOTS && !goalScored && !levelFailed) {
      const v = ball.velocity;
      const spd = Math.sqrt(v.x*v.x + v.y*v.y);
      if (spd < 0.5) {
        resetTimer += dt;
        if (resetTimer > 2.0) restartLevel();
      } else {
        resetTimer = 0;
      }
    }

    // Level 4 zero-g: reset after 6 seconds if 3 shots used
    if (curLevel === 3 && shots >= MAX_SHOTS && !goalScored && !levelFailed) {
      zeroGResetTimer += dt;
      if (zeroGResetTimer > 6) restartLevel();
    }
  }

  render();
  requestAnimationFrame(update);
}

// ── Rendering ─────────────────────────────────────────────────
function render() {
  ctx.clearRect(0, 0, GW, GH);

  if (gameState === S.MENU || gameState === S.PAUSED) {
    drawMenuBackground();
    return;
  }
  if (gameState === S.PLAYING || gameState === S.COMPLETE) {
    renderGame();
  }
}

function drawMenuBackground() {
  if (images.menuBg) {
    ctx.drawImage(images.menuBg, 0, 0, GW, GH);
  } else {
    ctx.fillStyle = '#1a1a2e';
    ctx.fillRect(0, 0, GW, GH);
  }
}

function renderGame() {
  // Background first
  const bgObj = levelBodies.find(o => o.type === 'bg');
  if (bgObj && images[bgObj.imageKey]) {
    ctx.drawImage(images[bgObj.imageKey], 0, 0, GW, GH);
  } else {
    ctx.fillStyle = '#222';
    ctx.fillRect(0, 0, GW, GH);
  }

  // Draw all bodies
  levelBodies.forEach(o => {
    if (o.type === 'bg' || o.type === 'invisible') return;
    if (o.body) drawBody(o);
  });

  // Explosion
  if (showExplosion) {
    const imgKey = curLevel === 2 ? 'goalPopup' : 'explosion';
    const img = images[imgKey];
    if (img) {
      const sz = 220;
      ctx.drawImage(img, explosionX - sz/2, explosionY - sz/2, sz, sz);
    } else {
      ctx.save();
      ctx.globalAlpha = 0.8;
      ctx.fillStyle = '#ff8800';
      ctx.beginPath();
      ctx.arc(explosionX, explosionY, 120, 0, Math.PI*2);
      ctx.fill();
      ctx.restore();
    }
  }

  // Aiming line
  if (pressPoint && dragPoint && !goalScored && shots < MAX_SHOTS) {
    ctx.save();
    ctx.strokeStyle = 'rgba(255,50,50,0.9)';
    ctx.lineWidth = 3;
    ctx.setLineDash([12, 8]);
    ctx.beginPath();
    ctx.moveTo(pressPoint.x, pressPoint.y);
    ctx.lineTo(dragPoint.x,  dragPoint.y);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.restore();
  }

  // HUD
  drawHUD();
}

function drawBody(o) {
  const b   = o.body;
  const pos = b.position;
  const ang = b.angle;

  ctx.save();
  ctx.translate(pos.x, pos.y);
  ctx.rotate(ang);

  const img = o.imageKey && images[o.imageKey];

  if (o.type === 'goal' && !img) {
    // wormhole visualisation
    ctx.fillStyle = o.color || 'rgba(100,0,255,0.25)';
    const r = b.circleRadius || 40;
    ctx.beginPath(); ctx.arc(0, 0, r, 0, Math.PI*2); ctx.fill();
    ctx.restore();
    return;
  }

  if (img) {
    const bnd = b.bounds;
    const w   = bnd.max.x - bnd.min.x;
    const h   = bnd.max.y - bnd.min.y;
    if (b.circleRadius) {
      const r = b.circleRadius;
      ctx.drawImage(img, -r, -r, r*2, r*2);
    } else {
      ctx.drawImage(img, -w/2, -h/2, w, h);
    }
  } else if (o.color && o.color !== 'rgba(0,0,0,0)') {
    ctx.fillStyle = o.color;
    const bnd = b.bounds;
    const w   = bnd.max.x - bnd.min.x;
    const h   = bnd.max.y - bnd.min.y;
    ctx.fillRect(-w/2, -h/2, w, h);
  }

  ctx.restore();
}

function drawHUD() {
  ctx.save();
  ctx.font = 'bold 32px sans-serif';

  // Shot counter
  const shotsLeft = MAX_SHOTS - shots;
  let shotText, shotColor;
  if (curLevel === 5 && shots >= MAX_SHOTS) {
    shotText  = "It's useless, Goku just can't be beaten";
    shotColor = '#ff2222';
  } else {
    shotText  = `Used ${shots} ${shots === 1 ? 'Shot' : 'Shots'} of ${MAX_SHOTS}`;
    shotColor = shots >= MAX_SHOTS ? '#ff4444' : '#ffffff';
  }
  ctx.fillStyle = 'rgba(0,0,0,0.5)';
  ctx.fillRect(10, 10, 560, 50);
  ctx.fillStyle = shotColor;
  ctx.fillText(shotText, 22, 45);

  // Score
  ctx.fillStyle = '#ffffff';
  ctx.font = 'bold 28px sans-serif';
  ctx.fillText(`Score: ${totalScore}`, 22, 85);
  ctx.fillText(`Level ${curLevel + 1} / 6`, 22, 118);

  // Git hash (version indicator)
  ctx.fillStyle = 'rgba(255,255,255,0.35)';
  ctx.font = '18px monospace';
  ctx.textAlign = 'right';
  ctx.fillText(GIT_HASH.slice(0, 7), GW - 10, GH - 10);
  ctx.textAlign = 'left';

  // Back button
  if (images.backBtn) {
    ctx.drawImage(images.backBtn, BACK_BTN.x, BACK_BTN.y, BACK_BTN.w, BACK_BTN.h);
  } else {
    ctx.fillStyle = 'rgba(50,50,50,0.8)';
    ctx.fillRect(BACK_BTN.x, BACK_BTN.y, BACK_BTN.w, BACK_BTN.h);
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 28px sans-serif';
    ctx.fillText('← Menu', BACK_BTN.x + 60, BACK_BTN.y + 44);
  }

  // Level failed overlay
  if (levelFailed) {
    ctx.fillStyle = 'rgba(0,0,0,0.55)';
    ctx.fillRect(0, 0, GW, GH);
    ctx.fillStyle = '#ff4444';
    ctx.font = 'bold 80px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('BLADE HIT!', GW/2, GH/2 - 20);
    ctx.font = 'bold 40px sans-serif';
    ctx.fillStyle = '#ffffff';
    ctx.fillText('Restarting...', GW/2, GH/2 + 60);
    ctx.textAlign = 'left';
  }

  ctx.restore();
}

function showCompleteScreen() {
  const el = document.getElementById('completeOverlay');
  el.classList.add('show');
  document.getElementById('completeScore').textContent =
    `${playerName} scored ${totalScore} points!`;
}

// ── Menu wiring ───────────────────────────────────────────────
function initMenu() {
  const btnPlay      = document.getElementById('btnPlay');
  const btnHighscore = document.getElementById('btnHighscore');
  const btnResume    = document.getElementById('btnResume');
  const nameInput    = document.getElementById('nameInput');

  btnPlay.addEventListener('click', () => {
    const name = nameInput.value.trim();
    if (!name || name === 'your name') { alert('Please enter your name to play!'); return; }
    playerName = name;
    curLevel   = 0;
    totalScore = 0;
    clearLevel();
    initLevel(curLevel);
    document.getElementById('menuOverlay').classList.add('hidden');
    gameState = S.PLAYING;
  });

  btnHighscore.addEventListener('click', () => {
    const tbl = document.getElementById('hsTable');
    if (tbl.style.display === 'block') { tbl.style.display = 'none'; return; }
    const tbody = document.getElementById('hsTableBody');
    tbody.innerHTML = highscores.length
      ? highscores.map((h,i) =>
          `<tr><td>${i+1}. ${h.name}</td><td>${h.score}</td></tr>`
        ).join('')
      : '<tr><td colspan="2">No scores yet</td></tr>';
    tbl.style.display = 'block';
  });

  btnResume.addEventListener('click', () => {
    if (gameState === S.PAUSED) resumeGame();
  });
}

// ── Boot ──────────────────────────────────────────────────────
loadHighscores();
loadAssets().then(() => {
  initMenu();
  requestAnimationFrame(ts => { lastTime = ts; update(ts); });
});

})();
