"""Verify background payout without a connected game screen against a disposable H2 server.
Requires ACE_JAVA and ACE_H2. Does not access the user's MySQL.
"""
import os, json, time, uuid, pathlib, subprocess, tempfile, urllib.request, http.cookiejar
root=pathlib.Path(__file__).resolve().parents[1]
base='http://127.0.0.1:18088/api/'
env={**os.environ,'PORT':'18088','DB_URL':'jdbc:h2:mem:dtjobs;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1','DB_USER':'sa','DB_PASSWORD':'','CREATOR_USER':'creator','CREATOR_PASSWORD':'test-only-password','JWT_SECRET':'test-only-secret-at-least-thirty-two-characters','JOBS_ENABLED':'true'}
jar=http.cookiejar.CookieJar()
opener=urllib.request.build_opener(urllib.request.ProxyHandler({}),urllib.request.HTTPCookieProcessor(jar))
csrf=''
def call(path,body=None):
    request=urllib.request.Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers={'Content-Type':'application/json','X-Game-Client':'web','X-CSRF-Token':csrf})
    with opener.open(request,timeout=10) as response:return json.load(response)
with tempfile.TemporaryFile(mode='w+') as log:
    server=subprocess.Popen([env['ACE_JAVA'],'-Dloader.path='+env['ACE_H2'],'-cp',str(root/'release/super-ace-online-13.0.0.jar'),'org.springframework.boot.loader.launch.PropertiesLauncher'],cwd=root,env=env,stdout=log,stderr=log)
    try:
        for _ in range(120):
            try:call('health');break
            except Exception:time.sleep(.25)
        state=call('register',{'username':'jobs'+uuid.uuid4().hex[:10],'password':'123456','rePassword':'123456'});csrf=state['csrf']
        while True:
            table=call('dragon-tiger/table')
            if table['phase']=='BETTING' and table['secondsRemaining']>=5:break
            time.sleep(.2)
        before=state['balanceCents'];bets=[]
        for side in ['DRAGON','TIGER','TIE']:
            state=call('me?mode=LOBBY')
            bets.append(call('dragon-tiger/rounds?mode=LOBBY',{'requestId':str(uuid.uuid4()),'tableRoundId':table['roundId'],'side':side,'betCents':1000,'expectedRevision':state['revision']}))
        assert call('me?mode=LOBBY')['balanceCents']==before-3000
        assert all(b['outcome'] is None and b['payoutCents']==0 for b in bets)
        # No table, bets or history polling until after payout should already be credited.
        time.sleep(max(0,(table['bettingClosesAt']-int(time.time()*1000))/1000)+1.5)
        paid=call('me?mode=LOBBY')['balanceCents'];assert paid>before-3000, 'background payout was not credited'
        rows=call('dragon-tiger/bets?mode=LOBBY&roundId='+str(table['roundId']))
        assert paid==before-3000+sum(b['payoutCents'] for b in rows)
        assert call('me?mode=LOBBY')['balanceCents']==paid, 'double settlement'
        assert call('me?mode=CLUB')['balanceCents']==0
        print('PASS background jobs: stakes only before close; automatic payout without game polling; no double payout; Club isolated.')
    except Exception:
        log.seek(0);print(log.read());raise
    finally:server.terminate();server.wait(timeout=20)
