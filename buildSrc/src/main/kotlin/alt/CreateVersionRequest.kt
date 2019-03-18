package alt

data class CreateVersionRequest(val name: String, val desc: String? = null, val released: String? = null, val vcs_tag: String? = null)
