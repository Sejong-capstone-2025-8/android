import os
import requests
from pydub import AudioSegment
from dotenv import load_dotenv
import io
import json

load_dotenv()

API_KEY = os.getenv('ELEVENLABS_API_KEY')

URL = "https://api.elevenlabs.io/v1/voices/add"

# 보낼 오디오 파일 경로 리스트
audio_files = [r"C:\Users\user\Downloads\chanwoo park.mp3"]

# 파일을 튜플 목록으로 변환 (multipart/form-data 포맷에 맞게)
files = [('files', (open(f, 'rb'))) for f in audio_files]

file_name = os.path.basename(audio_files[0]).rsplit('.', 1)[0]

# 기타 폼 데이터
data = {
    'name': file_name,
    'description': 'This is a vocie of chanwoo park.',
    'remove_background_noise': 'true',
    'labels': '{"gender":"male", "age":"young", "state" : "formal", "accent":"korean"}'
}

voice_name = data["name"]

# 헤더 설정
headers = {
    'xi-api-key': API_KEY,
}

# POST 요청
response = requests.post(URL, headers=headers, files=files, data=data)

# 응답 출력

if response.status_code == 200:
    data = response.json()
    voice_id = data.get("voice_id")

    if voice_id:
        json_path = "voice_list.json"

        # 기존 json 파일이 있으면 로드, 없으면 빈 리스트로 시작
        if os.path.exists(json_path):
            with open(json_path, "r", encoding="utf-8") as f:
                voice_list = json.load(f)
        else:
            voice_list = []

        # voice_id 중복 방지하고 추가
        if voice_id not in voice_list:
            voice_list[voice_name] = voice_id

        # 파일에 저장
        with open(json_path, "w", encoding="utf-8") as f:
            json.dump(voice_list, f, indent=4)

        print(f"voice_id 저장 완료: {voice_id}")
    else:
        print("응답에 voice_id가 없음")
else:
    print(f"요청 실패: {response.status_code}")