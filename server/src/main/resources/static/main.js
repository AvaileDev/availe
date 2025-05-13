const form      = document.getElementById('nlip-form');
const input     = document.getElementById('prompt');
const button    = document.getElementById('send');
const replyEl   = document.getElementById('reply');

function toggleButton() {
    button.disabled = !input.value.trim();
}
input.addEventListener('input', toggleButton);

async function ask(question, source) {
    const base = source === 'external' ? '/chat/external' : '/chat';
    const url  = `${base}?q=${encodeURIComponent(question)}`;

    const res = await fetch(url);
    if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
    return res.text();
}

form.addEventListener('submit', async e => {
    e.preventDefault();
    const q = input.value.trim();
    if (!q) return;

    const source =
        form.querySelector('input[name="source"]:checked').value;

    button.disabled = true;
    replyEl.textContent = 'Loading…';

    try {
        replyEl.textContent = await ask(q, source);
    } catch (err) {
        replyEl.textContent = `Error: ${err.message}`;
    } finally {
        button.disabled = false;
        input.focus();
    }
});
