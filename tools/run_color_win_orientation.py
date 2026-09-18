"""Test win/receipt orientation using client assets from the actual release JAR.
Requires Playwright and Chromium (ACE_PLAYWRIGHT / ACE_CHROMIUM).
Uses UI fixtures; does not connect to or modify a database.
"""
import os, pathlib, socket, subprocess, tempfile, time, zipfile
root = pathlib.Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix='ace-win-ui-') as tmp:
    with zipfile.ZipFile(root/'release/super-ace-online-13.0.0.jar') as jar:
        for name in jar.namelist():
            if name.startswith('BOOT-INF/classes/static/') and not name.endswith('/'):
                p = pathlib.Path(tmp)/name.removeprefix('BOOT-INF/classes/static/')
                p.parent.mkdir(parents=True, exist_ok=True)
                p.write_bytes(jar.read(name))
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    server = subprocess.Popen(['python3', '-m', 'http.server', str(port), '--bind', '127.0.0.1', '--directory', tmp], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    try:
        for attempt in range(100):
            try:
                with socket.create_connection(('127.0.0.1', port), timeout=.1): break
            except OSError: time.sleep(.05)
        subprocess.run(['node', str(root/'tools/color_win_orientation_browser.cjs')], cwd=root, env={**os.environ, 'ACE_TEST_URL':f'http://127.0.0.1:{port}'}, check=True, timeout=120)
    finally:
        server.terminate()
        server.wait(timeout=10)
