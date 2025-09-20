// 画面 → モデルの座標系：
//   xPct: -35..35（左マイナス/右プラス）  width方向
//   yPct: 10..90  （上→下）                 height方向
const MIN_X=-35, MAX_X=35, MIN_Y=10, MAX_Y=90;

const board = document.querySelector('.board');
const slots = Array.from(document.querySelectorAll('.slot'));

function xyToLeftTop(xPct, yPct) {
  const left = ( (xPct - MIN_X) / (MAX_X - MIN_X) ) * 100; // 0..100%
  const top  = ( (yPct - MIN_Y) / (MAX_Y - MIN_Y) ) * 100; // 0..100%
  return {left, top};
}
function leftTopToXY(left, top) {
  const xPct = MIN_X + (left/100) * (MAX_X - MIN_X);
  const yPct = MIN_Y + (top /100) * (MAX_Y - MIN_Y);
  return {xPct, yPct};
}
function clamp(v, lo, hi){ return Math.max(lo, Math.min(hi, v)); }
function round2(n){ return Math.round(n*100)/100; }

// 初期配置
slots.forEach(slot=>{
  const x = parseFloat(slot.dataset.x);
  const y = parseFloat(slot.dataset.y);
  const {left, top} = xyToLeftTop(x, y);
  slot.style.left = left + '%';
  slot.style.top  = top  + '%';
});

// ドラッグ
let dragging = null;
let start = null;

slots.forEach(slot=>{
  slot.addEventListener('pointerdown', (e)=>{
    dragging = slot;
    slot.setPointerCapture(e.pointerId);
    const rect = board.getBoundingClientRect();
    const left = ((slot.offsetLeft) / rect.width) * 100;
    const top  = ((slot.offsetTop)  / rect.height)* 100;
    start = {pointerX: e.clientX, pointerY: e.clientY, left, top, rect};
  });
  slot.addEventListener('pointermove', (e)=>{
    if(!dragging) return;
    if(dragging !== slot) return;
    const dx = (e.clientX - start.pointerX) / start.rect.width * 100;
    const dy = (e.clientY - start.pointerY) / start.rect.height* 100;
    let left = clamp(start.left + dx, 0, 100);
    let top  = clamp(start.top  + dy, 0, 100);
    slot.style.left = left + '%';
    slot.style.top  = top  + '%';

    // 画面→モデル座標に変換し、hidden に書き戻す
    const {xPct, yPct} = leftTopToXY(left, top);
    slot.querySelector('input[name="xPct"]').value = round2(xPct);
    slot.querySelector('input[name="yPct"]').value = round2(yPct);
    slot.querySelector('.cx').textContent = round2(xPct);
    slot.querySelector('.cy').textContent = round2(yPct);
  });
  slot.addEventListener('pointerup', ()=>{ dragging = null; start = null; });
  slot.addEventListener('pointercancel', ()=>{ dragging = null; start = null; });
});

// プリセット適用
const presets = {
  "433":[{l:"RW",x:20,y:30},{l:"ST",x:0,y:27},{l:"LW",x:-20,y:30},
         {l:"RM",x:22,y:51},{l:"CM",x:0,y:53},{l:"LM",x:-22,y:51},
         {l:"RB",x:25,y:66},{l:"RCB",x:10,y:68},{l:"LCB",x:-10,y:68},{l:"LB",x:-25,y:66},
         {l:"GK",x:0,y:80}],
  "442":[{l:"RS",x:10,y:31},{l:"LS",x:-10,y:31},
         {l:"RM",x:22,y:49},{l:"RCM",x:8,y:51},{l:"LCM",x:-8,y:51},{l:"LM",x:-22,y:49},
         {l:"RB",x:25,y:66},{l:"RCB",x:10,y:68},{l:"LCB",x:-10,y:68},{l:"LB",x:-25,y:66},
         {l:"GK",x:0,y:80}],
};
document.getElementById('applyPreset').addEventListener('click', ()=>{
  const key = document.getElementById('preset').value;
  const ps = presets[key];
  if(!ps) return;
  slots.forEach((slot, i)=>{
    const p = ps[i];
    if(!p) return;
    slot.querySelector('.label').value = p.l;
    const {left, top} = xyToLeftTop(p.x, p.y);
    slot.style.left = left + '%';
    slot.style.top  = top  + '%';
    slot.querySelector('input[name="xPct"]').value = p.x;
    slot.querySelector('input[name="yPct"]').value = p.y;
    slot.querySelector('.cx').textContent = p.x;
    slot.querySelector('.cy').textContent = p.y;
  });
});