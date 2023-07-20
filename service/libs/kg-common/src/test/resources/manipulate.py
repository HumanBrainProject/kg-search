import json
import os


def recursive_check(element:dict):
    if "otherPublications" in element:
        element["otherPublications"] = None
    for k, v in element.items():
        if type(v) == list:
            for i in v:
                if type(i) == dict:
                    recursive_check(i)
        elif type(v) == dict:
            recursive_check(v)



for f in os.listdir("."):
    if f.endswith("_source.json"):
        with open(f, "r") as j:
            payload = json.load(j)
        for p in payload:
            if "usedInDatasets" in p:
                del p["usedInDatasets"]
        os.remove(f)
        with open(f, "w") as target:
            target.write(json.dumps(payload, indent=2))

    if f.endswith("_target.json"):
        with open(f, "r") as j:
            payload = json.load(j)
        recursive_check(payload)
        os.remove(f)
        with open(f, "w") as target:
            target.write(json.dumps(payload, indent=2))