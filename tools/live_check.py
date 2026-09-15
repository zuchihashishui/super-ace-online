"""Disposable HTTP + DOM smoke, with file H2 for restart persistence."""
import os,subprocess,time,json,pathlib,sys
from http_smoke import seed,Client
root=pathlib.Path(__file__).resolve().parents[1]
env={**os.environ,'PORT':'18084','DB_URL':'jdbc:h2:file:/tmp/ace-v13-dom;MODE=MySQL;DATABASE_TO_LOWER=TRUE','DB_USER':'sa','DB_PASSWORD':'','CREATOR_USER':'creator','CREATOR_PASSWORD':'temporary-creator-password','JWT_SECRET':'temporary-test-secret-at-least-32-characters','SPIN_COOLDOWN_MS':'0'}
base='http://127.0.0.1:18084';log=open('/tmp/ace-dom-live.log','w')
def start():
 p=subprocess.Popen([env['ACE_JAVA'],'-Dloader.path='+env['ACE_H2'],'-cp',str(root/'server/target/super-ace-online-13.0.0.jar'),'org.springframework.boot.loader.launch.PropertiesLauncher'],env=env,stdout=log,stderr=log)
 for i in range(120):
  try:
   if Client(base).call('health')['status']=='up':return p
  except Exception:pass
  if p.poll() is not None:raise RuntimeError('Server exited; see log')
  time.sleep(.25)
 raise RuntimeError('Server startup timeout')
server=start()
try:
 data=seed(base,'creator',env['CREATOR_PASSWORD']);pathlib.Path('/tmp/ace-dom-seed.json').write_text(json.dumps(data))
 print('PASS HTTP smoke',flush=True)
 subprocess.run(['node',str(root/'tools/client_smoke.cjs')],env={**env,'ACE_JSDOM':'/tmp/ace-dom/node_modules/jsdom','ACE_TEST_URL':base,'ACE_TEST_SEED':'/tmp/ace-dom-seed.json'},check=True)
 user=Client(base);me=user.login(data['player'],data['password']);before=user.call('rtp')['startsAt'];wallet=user.call('me')
 import uuid
 run=str(uuid.uuid4());user.call('auto/start',{'runId':run,'count':10,'betCents':wallet['lockedBetCents'] if wallet['freeSpins'] else 2000,'expectedRevision':wallet['revision'],'turbo':True})
 server.terminate();server.wait(timeout=20);server=start();time.sleep(6)
 assert user.call('rtp')['startsAt']==before
 state=user.call('auto');assert state['job']['runId']==run;assert state['wallet']['revision']>wallet['revision'];user.call('auto/stop',{'runId':run})
 old=user.csrf;user.call('refresh',{});assert user.csrf!=old;assert user.call('me')['id']==me['id']
 print('PASS restart: JWT session, RTP epoch, wallet and server autoplay persist; refresh rotates.',flush=True)
finally:
 server.terminate();server.wait(timeout=20);log.close()
