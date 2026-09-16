"""Start a disposable H2 server and exercise registration/login/logout end-to-end.
Requires ACE_JAVA, ACE_H2, ACE_JSDOM (unless ACE_SKIP_DOM=1).
Never targets the user's MySQL instance.
"""
import os,json,time,uuid,pathlib,subprocess,tempfile,urllib.request,urllib.error,http.cookiejar
root=pathlib.Path(__file__).resolve().parents[1]
base='http://127.0.0.1:18085'
class Client:
 def __init__(self):
  self.jar=http.cookiejar.CookieJar();self.csrf='';self.opener=urllib.request.build_opener(urllib.request.ProxyHandler({}),urllib.request.HTTPCookieProcessor(self.jar))
 def call(self,path,data=None,expected=200,cookie=None):
  headers={'Content-Type':'application/json','X-Game-Client':'web','X-CSRF-Token':self.csrf}
  if cookie:headers['Cookie']=cookie
  req=urllib.request.Request(base+'/api/'+path,data=json.dumps(data).encode() if data is not None else None,headers=headers)
  try:r=self.opener.open(req,timeout=15)
  except urllib.error.HTTPError as e:r=e
  body=json.load(r);assert r.status==expected,(path,r.status,body)
  if 'csrf' in body:self.csrf=body['csrf']
  return body
env={**os.environ,'PORT':'18085','DB_URL':'jdbc:h2:mem:authcheck;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1','DB_USER':'sa','DB_PASSWORD':'','CREATOR_USER':'creator','CREATOR_PASSWORD':'test-creator-password','JWT_SECRET':'test-only-random-secret-at-least-32-characters','JOBS_ENABLED':'false','SPIN_COOLDOWN_MS':'0'}
with tempfile.TemporaryFile(mode='w+') as log:
 server=subprocess.Popen([env['ACE_JAVA'],'-Dloader.path='+env['ACE_H2'],'-cp',str(root/'release/super-ace-online-13.0.0.jar'),'org.springframework.boot.loader.launch.PropertiesLauncher'],env=env,stdout=log,stderr=log)
 try:
  guest=Client()
  for i in range(120):
   try:guest.call('health');break
   except Exception:
    if server.poll() is not None:log.seek(0);raise RuntimeError(log.read())
    time.sleep(.25)
  else:raise RuntimeError('Server did not start')
  name='p'+uuid.uuid4().hex[:12];credentials={'username':name,'password':'123456','rePassword':'123456'}
  guest.call('me',expected=401)
  guest.call('register',{'username':name,'password':'12345'},400)
  guest.call('register',{'username':name,'password':'界'*25},400)
  guest.call('register',{**credentials,'role':'CREATOR'},400)
  guest.call('register',{**credentials,'rePassword':'wrong6'},400)
  guest.call('register',{'username':name,'password':'123456'},400)
  me=guest.call('register',credentials);assert me['role']=='PLAYER';assert me['displayName']==name;assert len(me['publicCode'])==6 and me['publicCode'].isdigit();assert guest.call('me')['id']==me['id']
  assert me['mode']=='LOBBY' and me['lobbyGoldCents']==1000000 and me['clubChipsCents']==0 and me['clubCode']=='686868'
  club=guest.call('me?mode=CLUB');assert club['balanceCents']==0 and club['rtpProfile'] is None
  guest.call('rtp?mode=LOBBY',expected=403);guest.call('rtp?mode=CLUB',expected=403)
  guest.call('me?mode=INVALID',expected=400)
  tokens={c.name:c.value for c in guest.jar};assert len(tokens['ACE_SESSION'].split('.'))==3
  guest.call('register',credentials,409)
  guest.call('register',{'username':name.upper(),'password':'123456','rePassword':'123456'},409)
  guest.call('logout',{});assert not list(guest.jar);guest.call('me',expected=401)
  Client().call('me',expected=401,cookie='ACE_SESSION='+tokens['ACE_SESSION'])
  Client().call('refresh',{},401,cookie='ACE_REFRESH='+tokens['ACE_REFRESH'])
  guest.call('login',{'username':name,'password':'wrong6'},401)
  assert guest.call('login',{'username':name,'password':'123456'})['id']==me['id']
  assert guest.call('refresh',{})['id']==me['id']
  guest.call('logout',{})
  creator=Client();creator.call('login',{'username':env['CREATOR_USER'],'password':env['CREATOR_PASSWORD']});rows=creator.call('accounts');defaults={u['username']:u for u in rows};assert all(defaults[u]['role']==r for u,r in [('zuchiha1','SUPER_AGENT'),('zuchiha2','AGENT'),('zuchiha3','PLAYER')]);assert defaults['zuchiha3']['parentId']==defaults['zuchiha2']['id'];assert next(u for u in rows if u['id']==me['id'])['parentId']==defaults['zuchiha2']['id']
  assert creator.call('rtp?mode=LOBBY')['targetPercent']==100
  assert creator.call('rtp?mode=CLUB')['targetPercent']==97.5
  from dual_mode_checks import check_modes
  check_modes(Client,creator,guest,name,me)
  bettor=Client();bettor.call('register',{'username':'bet'+uuid.uuid4().hex[:12],'password':'123456','rePassword':'123456'})
  for mode in ['LOBBY','CLUB']:
   for invalid in [0,499,50001,-500]:
    state=bettor.call('me?mode='+mode)
    bettor.call('spins?mode='+mode,{'requestId':str(uuid.uuid4()),'betCents':invalid,'expectedRevision':state['revision']},400)
    bettor.call('auto/start?mode='+mode,{'runId':str(uuid.uuid4()),'count':10,'betCents':invalid,'expectedRevision':state['revision'],'turbo':False},400)
  state=bettor.call('me?mode=LOBBY')
  creator.call('chips/move',{'requestId':str(uuid.uuid4()),'targetId':state['id'],'direction':'GIVE','amountCents':100000})
  for mode in ['LOBBY','CLUB']:
   for wager in [500,755,50000]:
    state=bettor.call('me?mode='+mode)
    while state['freeSpins']:
     bettor.call('spins?mode='+mode,{'requestId':str(uuid.uuid4()),'betCents':state['lockedBetCents'],'expectedRevision':state['revision']})
     state=bettor.call('me?mode='+mode)
    req={'requestId':str(uuid.uuid4()),'betCents':wager,'expectedRevision':state['revision']}
    result=bettor.call('spins?mode='+mode,req)
    assert result['betCents']==wager and result['balanceBeforeCents']==state['balanceCents']
    assert bettor.call('spins?mode='+mode,req)==result
   state=bettor.call('me?mode='+mode)
   wager=state['lockedBetCents'] if state['freeSpins'] else 755
   run=str(uuid.uuid4())
   job=bettor.call('auto/start?mode='+mode,{'runId':run,'count':10,'betCents':wager,'expectedRevision':state['revision'],'turbo':False})
   assert job['active']
   bettor.call('auto/stop?mode='+mode,{'runId':run})
  print('PASS custom bets: 5, 7.55, 500 in both modes; invalid manual/auto bets rejected; idempotent retries.',flush=True)
  print('PASS HTTP: register 6 chars/no referral; invalid/duplicate/forged fields; generated Player ID; login; wrong password; refresh; logout and token revocation; direct hierarchy.',flush=True)
  if env.get('ACE_SKIP_DOM')=='1':
   print('SKIP DOM: backend-only run (ACE_SKIP_DOM=1).',flush=True)
  else:
   subprocess.run(['node',str(root/'tools/simple_auth_dom.cjs')],env={**env,'ACE_TEST_URL':base},check=True)
  if env.get('ACE_BROWSER_CHECK'):
   subprocess.run(['node',str(root/'tools/dragon_tiger_browser.cjs')],env={**env,'ACE_TEST_URL':base},check=True)
 finally:
  server.terminate();server.wait(timeout=20)
