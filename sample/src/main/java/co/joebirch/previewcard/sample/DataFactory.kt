package co.joebirch.previewcard.sample

object DataFactory {

    fun makeCarouselItem(): List<TvItem> {
        return listOf(
            TvItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerMeltdowns.jpg", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"),
            TvItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            TvItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerJoyrides.jpg", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"),
            TvItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg", "https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"),
            TvItem("https://storage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
        )
    }
}