from TwitterAPI import *
from credential import *
from datetime import datetime
import time

#the file credential.py has the variables with the consumer and access
api = TwitterAPI(consumer_key, consumer_secret, access_token, access_token_secret)
since_id = 1230077874998325248
count = 1
id = 0
f=open("greta.json", "a+")
while count > 0:
	r = api.request('search/tweets', {'q':'Think of your own children when you read this. Will you be the first generation ever not willing to sacrifice your own comfort to ensure a future for them? Because that\'s where we are right now... And this is why we strike from school.'
									 #,'count' : 100
									 ,'since_id' : since_id})
	created_at = ''
	for item in r:
		count+=1
		id = item['id']
		if id > since_id:
			since_id = id
			#print("id  %i", since_id)
		created_at = item['created_at']
		search_metadata = item['search_metadata']
		#just need the field date of retweet
		second = datetime.strptime(created_at, '%a %b %d %H:%M:%S +0000 %Y')
		ts = datetime.strftime(second,'%Y-%m-%d %H:%M:%S')

		print("{\"date\" : %s, \"seconds\" : %i, \"id\": %i } count %i  since_id %i search_metadata %s"
				% (ts, time.mktime(second.timetuple()), id,count, since_id, search_metadata))
		f.write("{\"date\" : %s, \"seconds\" : %i, \"id\": %i }\r\n " % (ts, time.mktime(second.timetuple()), id))
