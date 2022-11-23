package com.mparticle.kits

import com.mparticle.MParticle.IdentityType
import com.mparticle.identity.IdentityApiRequest

class FilteredIdentityApiRequest internal constructor(
    identityApiRequest: IdentityApiRequest?,
    provider: KitIntegration
) {
    var provider: KitIntegration

    var userIdentities: Map<IdentityType, String?> = HashMap()
        get() {
            val identities = field
            val filteredIdentities: MutableMap<IdentityType, String?> = HashMap(identities.size)
            for ((key, value) in identities) {
                if (provider.configuration?.shouldSetIdentity(key) == true) {
                    filteredIdentities[key] = value
                }
            }
            return filteredIdentities
        }

    init {
        if (identityApiRequest != null) {
            userIdentities = HashMap(identityApiRequest.userIdentities)
            if (provider.kitManager != null) {
                userIdentities =
                    provider.kitManager!!.dataplanFilter?.transformIdentities(userIdentities)!!
            }
        }
        this.provider = provider
    }

    @get:Deprecated("")
    val newIdentities: Map<IdentityType, String?>
        get() = userIdentities


}