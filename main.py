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

PARAGRAPHS = [
    "노는게 제일 좋아 친구들 모여라",
    "언제나! 즐거워! 오늘은 또 무슨 일이 생길까"
]
segments = []

for i, paragraph in enumerate(PARAGRAPHS):
    is_last_paragraph = i == len(PARAGRAPHS) - 1
    is_first_paragraph = i == 0
    response = requests.post(
        f"https://api.elevenlabs.io/v1/text-to-speech/{VOICE_ID}/stream",
        json={
            "text": paragraph,
            "model_id": "eleven_multilingual_v2",
            "previous_text": None if is_first_paragraph else " ".join(PARAGRAPHS[:i]),
            "next_text": None if is_last_paragraph else " ".join(PARAGRAPHS[i + 1:])
        },
        headers={"xi-api-key": API_KEY},
    )

    if response.status_code != 200:
        print(f"Error encountered, status: {response.status_code}, "
               f"content: {response.text}")
        quit()

    print(f"Successfully converted paragraph {i + 1}/{len(PARAGRAPHS)}")
    segments.append(AudioSegment.from_mp3(io.BytesIO(response.content)))

segment = segments[0]
for new_segment in segments[1:]:
    segment = segment + new_segment

audio_out_path = os.path.join(os.getcwd(), "with_text_conditioning.wav")
segment.export(audio_out_path, format="wav")
print(f"Success! Wrote audio to {audio_out_path}")

