import json

cluster_spec = '{"ps": ["amaterasu003.bigdata.zylk.net:46112"], "worker": ["amaterasu004.bigdata.zylk.net:37592", "amaterasu003.bigdata.zylk.net:46064"]}'
cluster_spec = json.loads(cluster_spec)
job_index = 0
job_type = "worker"

kkk = json.dumps({'cluster': cluster_spec,'task': {'type': job_type, 'index': job_index}})
print(kkk)