package cord.eoeo.momentwo.feature.albumdetail.impl

import cord.eoeo.momentwo.core.model.*

internal data class AlbumHeader(val id: Int, val title: String, val subTitle: String, val imageUrl: String)

internal sealed interface AlbumDetailContent { val permission: MemberAuth }
internal data class SubAlbumsContent(override val permission: MemberAuth, val items: List<SubAlbumItem>) : AlbumDetailContent
internal data class MembersContent(override val permission: MemberAuth, val items: List<MemberItem>) : AlbumDetailContent
internal data class SettingsContent(override val permission: MemberAuth) : AlbumDetailContent
