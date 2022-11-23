package com.mparticle.kits

import com.mparticle.MParticle
import com.mparticle.MParticle.IdentityType
import com.mparticle.TypedUserAttributeListener
import com.mparticle.UserAttributeListener
import com.mparticle.UserAttributeListenerType
import com.mparticle.consent.ConsentState
import com.mparticle.identity.MParticleUser
import com.mparticle.kits.KitConfiguration.Companion.filterAttributes

class FilteredMParticleUser private constructor(
    var mpUser: MParticleUser,
    var provider: KitIntegration
) : MParticleUser {
    override fun getId(): Long {
        return mpUser.id
    }

    /**
     * Retrieve filtered user identities. User this method to retrieve user identities at any time.
     * To ensure that filtering is respected, kits must use this method rather than the public API.
     *
     * @return a Map of identity-types and identity-values
     */
    override fun getUserAttributes(): Map<String, Any> {
        var userAttributes = mpUser.userAttributes
        val kitManager = provider.kitManager
        if (kitManager != null) {
            userAttributes = kitManager.dataplanFilter!!.transformUserAttributes(userAttributes)!!
        }
        return filterAttributes(
            provider.configuration?.userAttributeFilters,
            userAttributes as Map<String, List<String?>?>
        ) as Map<String, Any>
    }

    override fun getUserAttributes(listener: UserAttributeListenerType?): Map<String, Any>? {
        return mpUser.getUserAttributes(object : TypedUserAttributeListener {
            override fun onUserAttributesReceived(
                userAttributes: Map<String, Any?>,
                userAttributeLists: Map<String, List<String?>?>,
                mpid: Long
            ) {
                var userAttributes = userAttributes
                var userAttributeLists = userAttributeLists
                val kitManager = provider.kitManager
                if (kitManager != null) {
                    userAttributes =
                        kitManager.dataplanFilter!!.transformUserAttributes(userAttributes)!!
                    userAttributeLists =
                        kitManager.dataplanFilter!!.transformUserAttributes(userAttributeLists)!!
                }
                val filters = provider.configuration?.userAttributeFilters
                if (userAttributes == null) {
                    userAttributes = HashMap<String, Any>()
                }
                if (listener is UserAttributeListener) {
                    val stringifiedAttributes: MutableMap<String, String> = HashMap()
                    for ((key, value) in userAttributes) {
                        stringifiedAttributes[key] = value.toString()
                    }
                    listener.onUserAttributesReceived(
                        filterAttributes(filters, stringifiedAttributes) as MutableMap<String, String>?,
                        filterAttributes(
                            filters,
                            userAttributeLists
                        ) as MutableMap<String, List<String>>?,
                        mpid
                    )
                }
                if (listener is TypedUserAttributeListener) {
                    listener.onUserAttributesReceived(
                        filterAttributes(filters, userAttributes as Map<String, List<String?>?>)!!,
                        filterAttributes(
                            filters,
                            userAttributeLists
                        ) as Map<String, List<String?>?>,
                        mpid
                    )
                }
            }
        })
    }

    override fun setUserAttributes(userAttributes: Map<String, Any>): Boolean {
        return false
    }

    override fun getUserIdentities(): Map<IdentityType, String> {
        var identities = mpUser.userIdentities
        val kitManager = provider.kitManager
        if (kitManager != null) {
            identities = kitManager.dataplanFilter!!.transformIdentities(identities)!!
        }
        val filteredIdentities: MutableMap<IdentityType, String> = HashMap(
            identities.size
        )
        for ((key, value) in identities) {
            if (provider.configuration?.shouldSetIdentity(key) == true) {
                filteredIdentities[key] = value!!
            }
        }
        return filteredIdentities
    }

    override fun setUserAttribute(key: String, value: Any): Boolean {
        return false
    }

    override fun setUserAttributeList(key: String, value: Any): Boolean {
        return false
    }

    override fun incrementUserAttribute(key: String, value: Number): Boolean {
        return false
    }

    override fun removeUserAttribute(key: String): Boolean {
        return false
    }

    override fun setUserTag(tag: String): Boolean {
        return false
    }

    override fun getConsentState(): ConsentState? {
        return null
    }

    override fun setConsentState(state: ConsentState?) {}
    override fun isLoggedIn(): Boolean {
        return mpUser.isLoggedIn
    }

    override fun getFirstSeenTime(): Long {
        return mpUser.firstSeenTime
    }

    override fun getLastSeenTime(): Long {
        return mpUser.lastSeenTime
    }

    companion object {
        fun getInstance(user: MParticleUser?, provider: KitIntegration): FilteredMParticleUser? {
            return user?.let { FilteredMParticleUser(it, provider) }
        }

        fun getInstance(mpid: Long, provider: KitIntegration): FilteredMParticleUser? {
            val instance = MParticle.getInstance()
            if (instance != null) {
                val user = instance.Identity().getUser(mpid)
                if (user != null) {
                    return FilteredMParticleUser(user, provider)
                }
            }
            return null
        }
    }
}