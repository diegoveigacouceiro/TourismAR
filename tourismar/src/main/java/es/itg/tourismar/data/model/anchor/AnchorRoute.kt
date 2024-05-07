package es.itg.tourismar.data.model.anchor

data class AnchorRoute(
    val id: String = "",
    val anchorRouteName: String,
    val anchors: List<Anchor>,
    val imageUrl: String,
    val description: String
){
        constructor() : this("","", emptyList(), "", "")

}