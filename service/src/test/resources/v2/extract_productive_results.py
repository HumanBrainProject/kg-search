import json

with open("personSourceSample.jsonld") as file:
    payload = json.load(file)
    for result in payload["results"]:
        print(result["https://schema.hbp.eu/search/identifier"])
