package com.mparticle.networking

import android.util.MutableBoolean
import androidx.test.platform.app.InstrumentationRegistry
import com.mparticle.MParticle
import com.mparticle.identity.IdentityApiRequest
import com.mparticle.internal.AccessUtils
import com.mparticle.testutils.BaseCleanStartedEachTest
import com.mparticle.testutils.MPLatch
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.json.JSONObject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch

open class PinningTest : BaseCleanStartedEachTest() {
    lateinit var called: MutableBoolean
    lateinit var latch: CountDownLatch
    protected open fun shouldPin(): Boolean {
        return true
    }



    @Before
    fun before() {
        called = MutableBoolean(false)
        latch = MPLatch(1)
    }

    @Test
    @Throws(Exception::class)
    fun testIdentityClientLogin() {
        PinningTestHelper(mContext, "/login", object : PinningTestHelper.Callback {
            override fun onPinningApplied(pinned: Boolean) {
                assertEquals(shouldPin(), pinned)
                called.value = true
                latch.countDown()
            }
        })
        MParticle.getInstance()?.Identity()?.login(IdentityApiRequest.withEmptyUser().build())
        latch.await()
        assertTrue(called.value)
    }

    @Test
    @Throws(Exception::class)
    fun testIdentityClientLogout() {
        PinningTestHelper(mContext, "/logout", object : PinningTestHelper.Callback {
            override fun onPinningApplied(pinned: Boolean) {
                assertEquals(shouldPin(), pinned)
                called.value = true
                latch.countDown()
            }
        })
        MParticle.getInstance()?.Identity()?.logout(IdentityApiRequest.withEmptyUser().build())
        latch.await()
        assertTrue(called.value)
    }

    @Test
    @Throws(Exception::class)
    fun testIdentityClientIdentify() {
        PinningTestHelper(mContext, "/identify", object : PinningTestHelper.Callback {
            override fun onPinningApplied(pinned: Boolean) {
                assertEquals(shouldPin(), pinned)
                called.value = true
                latch.countDown()
            }
        })
        MParticle.getInstance()?.Identity()?.identify(IdentityApiRequest.withEmptyUser().build())
        latch.await()
        assertTrue(called.value)
    }

    @Test
    @Throws(Exception::class)
    fun testIdentityClientModify() {
        PinningTestHelper(mContext, "/modify", object : PinningTestHelper.Callback {
            override fun onPinningApplied(pinned: Boolean) {
                assertEquals(shouldPin(), pinned)
                called.value = true
                latch.countDown()
            }
        })
        MParticle.getInstance()?.Identity()?.modify(
            IdentityApiRequest.withEmptyUser().customerId(mRandomUtils.getAlphaNumericString(25))
                .build()
        )
        latch.await()
        assertTrue(called.value)
    }

    @Test
    @Throws(Exception::class)
    fun testMParticleClientFetchConfig() {
        try {
            PinningTestHelper(mContext, "/config", object : PinningTestHelper.Callback {
                override fun onPinningApplied(pinned: Boolean) {
                    assertEquals(shouldPin(), pinned)
                    called.value = true
                    latch.countDown()
                }
            })
            AccessUtils.getApiClient().fetchConfig(true)
        } catch (_: java.lang.Exception) {
        }
        latch.await()
        assertTrue(called.value)
    }

    @Test
    @Throws(Exception::class)
    fun testMParticleClientSendMessage() {
        PinningTestHelper(mContext, "/events", object : PinningTestHelper.Callback {
            override fun onPinningApplied(pinned: Boolean) {
                assertEquals(shouldPin(), pinned)
                called.value = true
                latch.countDown()
            }
        })
        try {
            AccessUtils.getApiClient().sendMessageBatch(JSONObject().toString())
        } catch (_: java.lang.Exception) {
        }
        latch.await()
        assertTrue(called.value)
    }

    companion object {
        @BeforeClass
        fun beforeClass() {
            MParticle.reset(InstrumentationRegistry.getInstrumentation().context)
        }
    }
    
}