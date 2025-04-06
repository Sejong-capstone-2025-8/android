package com.toprunner.imagestory.model

data class Theme(
    val id: String,
    val name: String,
    val description: String,
    val iconResource: Int
) {
    fun getDisplayName(): String {
        return name
    }

    fun getThemePrompt(): String {
        return when (id) {
            "fantasy" -> "이 이미지를 바탕으로 판타지 장르의 동화를 만들어주세요. 마법, 모험, 상상의 세계를 포함해주세요."
            "love" -> "이 이미지를 바탕으로 사랑에 관한 동화를 만들어주세요. 따뜻한 감정과 인간 관계를 중심으로 해주세요."
            "sf" -> "이 이미지를 바탕으로 공상과학(SF) 장르의 동화를 만들어주세요. 미래, 기술, 우주 등의 요소를 포함해주세요."
            "horror" -> "이 이미지를 바탕으로 무서운 요소가 있지만 아이들이 읽을 수 있는 약간 스릴 있는 동화를 만들어주세요."
            "comedy" -> "이 이미지를 바탕으로 유머러스하고 재미있는 동화를 만들어주세요. 웃음을 줄 수 있는 상황이나 캐릭터를 포함해주세요."
            else -> "이 이미지를 바탕으로 어린이를 위한 동화를 만들어주세요."
        }
    }
}