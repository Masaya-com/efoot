  (function () {
    const input = document.getElementById('itemInput');
    const chips = document.getElementById('chips');
    const hidden = document.getElementById('hiddenInputs');
    const count = document.getElementById('count');
    const addBtn = document.getElementById('addBtn');

    const MAX = 100;
    let items = [];

    const updateCount = () => { count.textContent = String(items.length); };

    const normalize = (s) => s.trim().replace(/\s+/g, ' ');

    const render = () => {
      chips.innerHTML = '';
      hidden.innerHTML = '';
      items.forEach((v, idx) => {
        const chip = document.createElement('span');
        chip.className = 'chip';
        chip.innerHTML = `<span>${escapeHtml(v)}</span> <button type="button" aria-label="削除">×</button>`;
        chip.querySelector('button').addEventListener('click', () => {
          items.splice(idx, 1);
          render();
        });
        chips.appendChild(chip);

        const h = document.createElement('input');
        h.type = 'hidden';
        h.name = 'items';
        h.value = v;
        hidden.appendChild(h);
      });
      updateCount();
    };

    const addCurrent = () => {
      const v = normalize(input.value);
      if (!v) return;
      if (items.length >= MAX) { alert('最大 100 件です'); return; }
      // 重複（大文字小文字/全半角空白）を許さない
      const lower = v.toLowerCase();
      if (items.some(x => x.toLowerCase() === lower)) { input.value=''; return; }
      items.push(v);
      input.value = '';
      render();
    };

    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') { e.preventDefault(); addCurrent(); }
    });
    addBtn.addEventListener('click', addCurrent);

    // 画面離脱時に値を保持（任意）
    window.addEventListener('beforeunload', () => {
      sessionStorage.setItem('efs_items', JSON.stringify(items));
      sessionStorage.setItem('efs_title', document.getElementById('title').value || '');
      const type = document.querySelector('input[name="type"]:checked')?.value || 'nationality';
      sessionStorage.setItem('efs_type', type);
    });

    // 復元
    window.addEventListener('DOMContentLoaded', () => {
      try {
        const saved = JSON.parse(sessionStorage.getItem('efs_items') || '[]');
        if (Array.isArray(saved)) { items = saved.slice(0, MAX); render(); }
        const t = sessionStorage.getItem('efs_title'); if (t) document.getElementById('title').value = t;
        const type = sessionStorage.getItem('efs_type');
        if (type) {
          const r = document.querySelector(`input[name="type"][value="${type}"]`);
          if (r) r.checked = true;
        }
      } catch {}
    });

    function escapeHtml(str){
      return str.replace(/[&<>"']/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[s]));
    }
  })();
