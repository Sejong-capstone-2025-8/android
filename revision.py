import os
import requests
from pydub import AudioSegment
from dotenv import load_dotenv
import io
import json

load_dotenv()

API_KEY = os.getenv('ELEVENLABS_API_KEY')

with open("voice_list.json", "r", encoding="utf-8") as f:
    voice_list = json.load(f)

# 원하는 값 불러오기
VOICE_ID = voice_list["chanwoo park"]  # 실제 voice_id 입력

url = f"https://api.elevenlabs.io/v1/voices/{VOICE_ID}/settings/edit"

# 수정할 설정값
payload = {
    "stability": 1.0,            # 0.0 ~ 1.0 (낮을수록 감정 다양성 ↑)
    "similarity_boost": 1.0,    # 0.0 ~ 1.0 (높을수록 원래 목소리와 비슷하게)
    "style": 1.0,                # 0.0 ~ 1.0 (스타일 강조, 감정 강조)
    "use_speaker_boost": True,   # 원래 목소리에 더 가깝게
    "speed": 0.9                # 0.7 ~ 1.2 (1.0이 기본, 더 빠르게 말하게 함)
}

# 헤더 구성
headers = {
    "xi-api-key": API_KEY,
    "Content-Type": "application/json"
}

# 요청 보내기
response = requests.post(url, headers=headers, json=payload)

# 결과 확인
if response.status_code == 200:
    print("설정 업데이트 성공")
    print(response.json())
else:
    print("설정 업데이트 실패:", response.status_code)
    print(response.text)
