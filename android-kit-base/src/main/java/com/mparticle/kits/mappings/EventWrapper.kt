package com.mparticle.kits.mappings

import com.mparticle.MPEvent
import com.mparticle.commerce.CommerceEvent
import com.mparticle.commerce.Product
import com.mparticle.commerce.Promotion
import com.mparticle.kits.KitUtils.hashForFiltering
import com.mparticle.kits.CommerceEventUtils.getEventType
import com.mparticle.kits.CommerceEventUtils.extractActionAttributes
import com.mparticle.kits.CommerceEventUtils.extractTransactionAttributes
import com.mparticle.kits.CommerceEventUtils.extractProductAttributes
import com.mparticle.kits.CommerceEventUtils.extractProductFields
import com.mparticle.kits.CommerceEventUtils.extractPromotionAttributes
import com.mparticle.kits.mappings.EventWrapper
import com.mparticle.kits.KitUtils
import com.mparticle.kits.CommerceEventUtils
import com.mparticle.kits.mappings.CustomMapping
import java.util.AbstractMap
import java.util.HashMap

/**
 * Decorator classes for MPEvent and CommerceEvent. Used to extend functionality and to cache values for Projection processing.
 */
internal abstract class EventWrapper {
    abstract fun getAttributeHashes(): Map<Int, String>
    protected var attributeHashes: MutableMap<Int, String>? = null
    abstract val eventTypeOrdinal: Int
    abstract val event: Any
    abstract val messageType: Int
    abstract val eventHash: Int
    abstract fun findAttribute(
        propertyType: String?,
        hash: Int,
        product: Product?,
        promotion: Promotion?
    ): Map.Entry<String, String?>?

    abstract fun findAttribute(
        propertyType: String?,
        keyName: String,
        product: Product?,
        promotion: Promotion?
    ): Map.Entry<String, String?>?

    internal class CommerceEventWrapper(private var mCommerceEvent: CommerceEvent) :
        EventWrapper() {
        private var eventFieldHashes: Map<Int, String>? = null
        private var eventFieldAttributes: HashMap<String, String>? = null
        override fun getAttributeHashes(): Map<Int, String> {
            if (attributeHashes == null) {
                attributeHashes = HashMap()
                if (mCommerceEvent.customAttributeStrings != null) {
                    for ((key) in mCommerceEvent.customAttributeStrings!!) {
                        val hash = hashForFiltering(getEventTypeOrdinal().toString() + key)
                        attributeHashes[hash] = key
                    }
                }
            }
            return attributeHashes!!
        }

        override fun getEventTypeOrdinal(): Int {
            return getEventType(mCommerceEvent)
        }

        override fun getEvent(): CommerceEvent {
            return mCommerceEvent
        }

        fun setEvent(event: CommerceEvent) {
            mCommerceEvent = event
        }

        override fun getMessageType(): Int {
            return 16
        }

        override fun getEventHash(): Int {
            return hashForFiltering("" + getEventTypeOrdinal())
        }

        override fun findAttribute(
            propertyType: String?,
            hash: Int,
            product: Product?,
            promotion: Promotion?
        ): Map.Entry<String, String?>? {
            if (CustomMapping.PROPERTY_LOCATION_EVENT_ATTRIBUTE.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (getEvent().customAttributeStrings == null || getEvent().customAttributeStrings!!.size == 0) {
                    return null
                }
                val key = getAttributeHashes()[hash]
                if (key != null) {
                    return AbstractMap.SimpleEntry(
                        key,
                        mCommerceEvent.customAttributeStrings!![key]
                    )
                }
            } else if (CustomMapping.PROPERTY_LOCATION_EVENT_FIELD.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (eventFieldHashes == null) {
                    if (eventFieldAttributes == null) {
                        eventFieldAttributes = HashMap()
                        extractActionAttributes(getEvent(), eventFieldAttributes)
                        extractTransactionAttributes(getEvent(), eventFieldAttributes)
                    }
                    eventFieldHashes =
                        getHashes(getEventTypeOrdinal().toString() + "", eventFieldAttributes!!)
                }
                val key = eventFieldHashes!![hash]
                if (key != null) {
                    return AbstractMap.SimpleEntry(key, eventFieldAttributes!![key])
                }
            } else if (CustomMapping.PROPERTY_LOCATION_PRODUCT_ATTRIBUTE.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (product == null || product.customAttributes == null || product.customAttributes!!.size == 0) {
                    return null
                }
                val attributes: Map<String, String> = HashMap()
                extractProductAttributes(product, attributes)
                val hashes = getHashes(getEventTypeOrdinal().toString() + "", attributes)
                val key = hashes[hash]
                if (key != null) {
                    return AbstractMap.SimpleEntry(key, attributes[key])
                }
            } else if (CustomMapping.PROPERTY_LOCATION_PRODUCT_FIELD.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (product == null) {
                    return null
                }
                val attributes: Map<String, String> = HashMap()
                extractProductFields(product, attributes)
                val hashes = getHashes(getEventTypeOrdinal().toString() + "", attributes)
                val key = hashes[hash]
                if (key != null) {
                    return AbstractMap.SimpleEntry(key, attributes[key])
                }
            } else if (CustomMapping.PROPERTY_LOCATION_PROMOTION_FIELD.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (promotion == null) {
                    return null
                }
                val attributes: Map<String, String> = HashMap()
                extractPromotionAttributes(promotion, attributes)
                val hashes = getHashes(getEventTypeOrdinal().toString() + "", attributes)
                val key = hashes[hash]
                if (key != null) {
                    return AbstractMap.SimpleEntry(key, attributes[key])
                }
            }
            return null
        }

        override fun findAttribute(
            propertyType: String?,
            keyName: String,
            product: Product?,
            promotion: Promotion?
        ): Map.Entry<String, String?>? {
            if (CustomMapping.PROPERTY_LOCATION_EVENT_ATTRIBUTE.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (getEvent().customAttributeStrings == null || getEvent().customAttributeStrings!!.size == 0) {
                    return null
                }
                if (getEvent().customAttributeStrings!!.containsKey(keyName)) {
                    return AbstractMap.SimpleEntry(
                        keyName,
                        getEvent().customAttributeStrings!![keyName]
                    )
                }
            } else if (CustomMapping.PROPERTY_LOCATION_EVENT_FIELD.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (eventFieldAttributes == null) {
                    eventFieldAttributes = HashMap()
                    extractActionAttributes(getEvent(), eventFieldAttributes)
                    extractTransactionAttributes(getEvent(), eventFieldAttributes)
                }
                if (eventFieldAttributes!!.containsKey(keyName)) {
                    return AbstractMap.SimpleEntry(keyName, eventFieldAttributes!![keyName])
                }
            } else if (CustomMapping.PROPERTY_LOCATION_PRODUCT_ATTRIBUTE.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (product == null || product.customAttributes == null) {
                    return null
                }
                val attributes: Map<String, String> = HashMap()
                extractProductAttributes(product, attributes)
                if (attributes.containsKey(keyName)) {
                    return AbstractMap.SimpleEntry(keyName, attributes[keyName])
                }
            } else if (CustomMapping.PROPERTY_LOCATION_PRODUCT_FIELD.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (product == null) {
                    return null
                }
                val attributes: Map<String, String> = HashMap()
                extractProductFields(product, attributes)
                if (attributes.containsKey(keyName)) {
                    return AbstractMap.SimpleEntry(keyName, attributes[keyName])
                }
            } else if (CustomMapping.PROPERTY_LOCATION_PROMOTION_FIELD.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (promotion == null) {
                    return null
                }
                val attributes: Map<String, String> = HashMap()
                extractPromotionAttributes(promotion, attributes)
                if (attributes.containsKey(keyName)) {
                    return AbstractMap.SimpleEntry(keyName, attributes[keyName])
                }
            }
            return null
        }
    }

    internal class MPEventWrapper @JvmOverloads constructor(
        private val mEvent: MPEvent,
        private val mScreenEvent: Boolean = false
    ) : EventWrapper() {
        override fun getAttributeHashes(): Map<Int, String> {
            if (attributeHashes == null) {
                attributeHashes = HashMap()
                if (mEvent.customAttributeStrings != null) {
                    for ((key) in mEvent.customAttributeStrings!!) {
                        val hash =
                            hashForFiltering(getEventTypeOrdinal().toString() + mEvent.eventName + key)
                        attributeHashes[hash] = key
                    }
                }
            }
            return attributeHashes!!
        }

        override fun getEvent(): MPEvent {
            return mEvent
        }

        override fun getEventTypeOrdinal(): Int {
            return if (mScreenEvent) {
                0
            } else {
                mEvent.eventType.ordinal
            }
        }

        override fun getEventHash(): Int {
            return if (mScreenEvent) {
                hashForFiltering(getEventTypeOrdinal().toString() + mEvent.eventName)
            } else {
                mEvent.eventHash
            }
        }

        override fun getMessageType(): Int {
            return if (mScreenEvent) {
                3
            } else {
                4
            }
        }

        override fun findAttribute(
            propertyType: String?,
            keyName: String,
            product: Product?,
            promotion: Promotion?
        ): Map.Entry<String, String?>? {
            if (CustomMapping.PROPERTY_LOCATION_EVENT_ATTRIBUTE.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                if (getEvent().customAttributeStrings == null) {
                    return null
                }
                val value = getEvent().customAttributeStrings!![keyName]
                if (value != null) {
                    return AbstractMap.SimpleEntry(keyName, value)
                }
            }
            return null
        }

        override fun findAttribute(
            propertyType: String?,
            hash: Int,
            product: Product?,
            promotion: Promotion?
        ): Map.Entry<String, String?>? {
            if (CustomMapping.PROPERTY_LOCATION_EVENT_ATTRIBUTE.equals(
                    propertyType,
                    ignoreCase = true
                )
            ) {
                val key = getAttributeHashes()[hash]
                if (key != null) {
                    return AbstractMap.SimpleEntry(key, mEvent.customAttributeStrings!![key])
                }
            }
            return null
        }
    }

    companion object {
        protected fun getHashes(hashPrefix: String, map: Map<String, String>): Map<Int, String> {
            val hashedMap: MutableMap<Int, String> = HashMap()
            for ((key) in map) {
                val hash = hashForFiltering(hashPrefix + key)
                hashedMap[hash] = key
            }
            return hashedMap
        }
    }
}