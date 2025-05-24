import os
import requests

BASE_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")

def chat_complete(messages, tools=None, model="granite3.3:2b", timeout=60, temperature=0):
    payload = {
        "model": model,
        "messages": messages,
        "stream": False,
        "temperature": temperature,
    }
    if tools is not None:
        payload["tools"] = tools
        payload["tool_choice"] = "auto"
    resp = requests.post(f"{BASE_URL}/v1/chat/completions", json=payload, timeout=timeout)
    resp.raise_for_status()
    return resp.json()