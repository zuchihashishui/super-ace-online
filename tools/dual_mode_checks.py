import uuid

def check_modes(Client, creator, player, name, me):
    player.call('login', {'username':name,'password':'123456'})
    def uid(): return str(uuid.uuid4())
    def spin(mode, revision, request=None):
        return player.call('spins?mode='+mode, {'requestId':request or uid(),'betCents':5000,'expectedRevision':revision})
    body={'requestId':uid(),'betCents':5000,'expectedRevision':0}
    player.call('spins?mode=CLUB',body,409)
    first=spin('LOBBY',0,body['requestId'])
    replay=spin('LOBBY',0,body['requestId'])
    assert replay==first
    assert player.call('me?mode=CLUB')['balanceCents']==0
    assert player.call('reports?period=week')['wagerCents']==0
    def account(role,parent):
        n='mode_'+uuid.uuid4().hex[:12]
        return creator.call('accounts',{'username':n,'displayName':n,'password':'123456','role':role,'parentId':parent})
    owner=creator.call('me')
    sa=account('SUPER_AGENT',owner['id']);agent=account('AGENT',sa['id'])
    outsider=account('AGENT',sa['id'])
    # Create a child in the new agent's branch; public signup stays in Lucky Seven's default branch.
    child=account('PLAYER',agent['id'])
    agent_client=Client();agent_client.call('login',{'username':agent['username'],'password':'123456'})
    super_client=Client();super_client.call('login',{'username':sa['username'],'password':'123456'})
    def move(client,target,amount,request=None,direction='GIVE',expected=200):
        return client.call('chips/move',{'requestId':request or uid(),'targetId':target,'direction':direction,'amountCents':amount},expected)
    move(agent_client,child['id'],100,expected=409)
    move(super_client,agent['id'],100,expected=409)
    move(agent_client,me['id'],100,expected=403)
    move(agent_client,child['id'],100,direction='ISSUE',expected=403)
    move(player,child['id'],100,expected=403)
    move(creator,sa['id'],10000)
    move(super_client,agent['id'],6000)
    movement=uid();move(agent_client,child['id'],4000,movement);move(agent_client,child['id'],4000,movement)
    assert super_client.call('me')['clubChipsCents']==4000
    assert agent_client.call('me')['clubChipsCents']==2000
    child_client=Client();child_me=child_client.call('login',{'username':child['username'],'password':'123456'})
    assert child_me['lobbyGoldCents']==1000000 and child_me['clubChipsCents']==4000
    move(agent_client,child['id'],2001,expected=409)
    move(agent_client,child['id'],1000,direction='TAKE')
    assert agent_client.call('me')['clubChipsCents']==3000
    assert child_client.call('me')['clubChipsCents']==3000
    assert creator.call('me')['clubChipsCents']==owner['clubChipsCents']
    move(creator,me['id'],50000)
    before=player.call('me?mode=LOBBY');club=player.call('me?mode=CLUB')
    result=spin('CLUB',club['revision'],body['requestId'])
    assert result['balanceCents']==50000-5000+result['outcome']['winCents']
    assert player.call('me?mode=LOBBY')['balanceCents']==before['balanceCents']
    assert player.call('reports?period=week')['wagerCents']==5000
    notices=child_client.call('notifications');assert len(notices)==1 and notices[0]['amountCents']==4000
    player.call('notifications/'+notices[0]['id']+'/read',{},403)
    child_client.call('notifications/'+notices[0]['id']+'/read',{});assert child_client.call('notifications')==[]
    child_client.call('notifications/'+notices[0]['id']+'/read',{})
    player.call('chips',{'requestId':uid(),'kind':'DEPOSIT','amountCents':100,'reference':'test'},403)
    creator.call('chips/decide',{'id':uid(),'approve':True},403)
    for mode in ('LOBBY','CLUB'):
        w=player.call('me?mode='+mode)
        job=player.call('auto/start?mode='+mode,{'runId':uid(),'count':10,'betCents':5000,'expectedRevision':w['revision'],'turbo':True})
        assert player.call('auto?mode='+mode)['job']['runId']==job['runId']
        player.call('auto/stop?mode='+mode,{'runId':job['runId']})
    assert child_client.call('me')['lobbyGoldCents']==1000000
    print('PASS modes: 10,000 Gold/0 chips, isolated spins/retries/reports, Creator mint, funded hierarchy transfers, denied overdrafts/outsiders/Player issuance, independent autoplay endpoints.',flush=True)
