"""Run actual release-JAR browser checks against isolated H2 databases.
Requires Java 21, H2 test JAR, Node.js, Playwright and Chromium.
Set ACE_JAVA, ACE_H2_JAR, ACE_PLAYWRIGHT and ACE_CHROMIUM if not on defaults.
Usage: python tools/run_browser_checks.py [bingo lucky9 ...]
Never points at the user's MySQL database.
"""
import os, pathlib, shutil, socket, subprocess, sys, tempfile, time, urllib.request, uuid
ROOT=pathlib.Path(__file__).resolve().parents[1]
SUITES={'plinko-motion':'plinko_motion_browser.cjs','color27':'color_v27_browser.cjs','superace':'superace_collection_browser.cjs','sakla':'sakla_browser.cjs','lucky9':'lucky_nine_browser.cjs','bingo':'bingo_browser.cjs','plinko':'arcade_browser.cjs','wheel':'wheel_browser.cjs','slots':'slots_browser.cjs','mines':'mines_browser.cjs','crash':'crash_browser.cjs','dragon-tiger':'dragon_tiger_browser.cjs','color':'color_game_browser.cjs','recovery':'color_game_recovery_browser.cjs'}
def port():
 with socket.socket() as s:s.bind(('127.0.0.1',0));return s.getsockname()[1]
def main():
 selected=sys.argv[1:] or list(SUITES)
 if any(s not in SUITES for s in selected):raise SystemExit('Suites: '+', '.join(SUITES))
 java=os.environ.get('ACE_JAVA') or (str(pathlib.Path(os.environ['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java')) if 'JAVA_HOME' in os.environ else shutil.which('java'))
 h2=os.environ.get('ACE_H2_JAR',str(pathlib.Path.home()/'.m2/repository/com/h2database/h2/2.3.232/h2-2.3.232.jar'))
 if not java or not pathlib.Path(h2).is_file():raise SystemExit('Set ACE_JAVA to Java 21 and ACE_H2_JAR to the H2 2.3.232 test JAR.')
 opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
 with tempfile.TemporaryDirectory(prefix='ace-browser-') as tmp:
  for suite in selected:
   http,tcp=port(),port();dburl=f'jdbc:h2:tcp://127.0.0.1:{tcp}/mem:test{uuid.uuid4().hex};MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1'
   env={**os.environ,'PORT':str(http),'DB_URL':dburl,'DB_USER':'sa','DB_PASSWORD':'','CREATOR_USER':'creator','CREATOR_PASSWORD':'test-creator-password','JWT_SECRET':'test-only-secret-at-least-32-characters-long','JOBS_ENABLED':'true' if suite=='crash' else 'false','SPIN_COOLDOWN_MS':'0','ACE_TEST_URL':f'http://127.0.0.1:{http}','ACE_JAVA':java,'ACE_H2_JAR':h2}
   logfile=pathlib.Path(tmp)/(suite+'.log');processes=[]
   print('RUN',suite,flush=True)
   try:
    with logfile.open('w') as log:
     db=subprocess.Popen([java,'-cp',h2,'org.h2.tools.Server','-tcp','-tcpPort',str(tcp),'-ifNotExists'],stdout=log,stderr=log);processes.append(db)
     app=subprocess.Popen([java,'-Dloader.path='+h2,'-cp',str(ROOT/'release/super-ace-online-13.0.0.jar'),'org.springframework.boot.loader.launch.PropertiesLauncher'],cwd=ROOT,env=env,stdout=log,stderr=log);processes.append(app)
     deadline=time.monotonic()+50
     while True:
      if app.poll() is not None:raise RuntimeError('Release JAR exited during startup')
      try:
       opener.open(env['ACE_TEST_URL']+'/api/health',timeout=1).close();break
      except OSError:
       if time.monotonic()>deadline:raise RuntimeError('Release JAR startup timed out')
       time.sleep(.2)
     subprocess.run(['node',str(ROOT/'tools'/SUITES[suite])],cwd=ROOT,env=env,check=True,timeout=180)
   except Exception:
    print(logfile.read_text()[-6000:],file=sys.stderr);raise
   finally:
    for p in reversed(processes):
     p.terminate()
     try:p.wait(timeout=15)
     except subprocess.TimeoutExpired:p.kill();p.wait()
 print('ALL SELECTED BROWSER CHECKS PASSED',flush=True)
if __name__=='__main__':main()
