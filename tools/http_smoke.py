"""Smoke checks against a disposable test server. Never target production.
Set ACE_TEST_URL, ACE_TEST_CREATOR, ACE_TEST_PASSWORD. Creates a test hierarchy.
"""
import http.cookiejar,json,os,urllib.request,urllib.error,uuid
class Client:
 def __init__(self,base):
  self.base=base;self.csrf='';self.opener=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
 def call(self,path,data=None):
  headers={'Content-Type':'application/json','X-Game-Client':'web','X-CSRF-Token':self.csrf}
  r=urllib.request.Request(self.base+'/api/'+path,data=None if data is None else json.dumps(data).encode(),headers=headers)
  with self.opener.open(r,timeout=20) as response:body=json.load(response)
  if isinstance(body,dict) and 'csrf' in body:self.csrf=body['csrf']
  return body
 def login(self,user,password):return self.call('login',{'username':user,'password':password})
def seed(base,username,password):
 root=Client(base);me=root.login(username,password);suffix=uuid.uuid4().hex[:10];testpass='temporary-smoke-password'
 def create(role,parent,name):return root.call('accounts',{'username':name+'_'+suffix,'displayName':name.title()+' '+suffix,'password':testpass,'role':role,'parentId':parent})
 sa=create('SUPER_AGENT',me['id'],'super');agent=create('AGENT',sa['id'],'agent');player=create('PLAYER',agent['id'],'player')
 user=Client(base);user.login(player['username'],testpass)
 # Same round request submitted twice has a single wallet revision and single debit.
 for _ in range(10):
  wallet=user.call('me');bet=wallet['lockedBetCents'] if wallet['freeSpins'] else 2000
  payload={'requestId':str(uuid.uuid4()),'betCents':bet,'expectedRevision':wallet['revision']}
  result=user.call('spins',payload);assert user.call('spins',payload)==result
  assert result['balanceCents']==wallet['balanceCents']-(0 if result['freeSpin'] else bet)+result['outcome']['winCents']
  import time;time.sleep(.26)
 transfer=user.call('chips',{'requestId':str(uuid.uuid4()),'kind':'WITHDRAWAL','amountCents':1000,'reference':'smoke refund'})
 before=user.call('me')['balanceCents'];root.call('chips/decide',{'id':transfer['id'],'approve':False});root.call('chips/decide',{'id':transfer['id'],'approve':False});assert user.call('me')['balanceCents']==before+1000
 ranking=user.call('ranking?period=week');assert len(ranking['players'])<=10
 report=user.call('reports?period=week');assert report['netCents']==report['payoutCents']-report['wagerCents'];assert len(report['players'])==1
 return {'player':player['username'],'password':testpass,'agentCode':agent['publicCode'],'agent':agent['username'],'root':username,'rootPassword':password,'playerId':player['id']}
if __name__=='__main__':
 result=seed(os.environ['ACE_TEST_URL'],os.environ['ACE_TEST_CREATOR'],os.environ['ACE_TEST_PASSWORD']);print('PASS: login, hierarchy, 10 spins + 10 exact replays, withdrawal refund, reports, ranking')
