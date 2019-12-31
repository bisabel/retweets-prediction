from TwitterAPI import *
from credential import *

#the file credential.py has the variables with the consumer and access
api = TwitterAPI(consumer_key, consumer_secret, access_token, access_token_secret)

#tweet uso to extract retweets:
#https://twitter.com/yesjimstheman/status/1211355167855169536
r = api.request('statuses/retweets/:%d' % 1211355167855169536, {'count': 100})
created_at = ''
id = ''
for item in r:
	id = item['id']
	created_at = item['created_at']
	#just need the field date of retweet
	print(item['created_at'])
	#comment next line, used for test
	#print("Date: %s  id: %i" % (created_at, id))
 
