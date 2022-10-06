package com.mparticle.internal

import android.os.Message
import org.powermock.modules.junit4.PowerMockRunner
import com.mparticle.internal.ConfigManager
import com.mparticle.internal.MessageManager
import com.mparticle.internal.database.services.MParticleDBManager
import com.mparticle.MParticle
import com.mparticle.MockMParticle
import com.mparticle.internal.AppStateManager
import com.mparticle.mock.MockContext
import com.mparticle.testutils.AndroidUtils
import com.mparticle.internal.database.MPDatabase
import com.mparticle.identity.AliasRequest
import com.mparticle.testutils.TestingUtils
import com.mparticle.internal.messages.MPAliasMessage
import com.mparticle.internal.Constants.MessageKey
import junit.framework.TestCase
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.lang.Exception

@RunWith(PowerMockRunner::class)
class MessageHandlerTest {
    var mConfigManager: ConfigManager? = null
    private var handler: MessageHandler? = null
    var mMessageManager: MessageManager? = null
    var mParticleDatabaseManager: MParticleDBManager? = null
    @Before
    @Throws(Exception::class)
    fun setUp() {
        MParticle.setInstance(MockMParticle())
        mConfigManager = MParticle.getInstance()?.Internal()?.configManager
        Mockito.`when`(Mockito.mock(MessageManager::class.java).apiKey).thenReturn("apiKey")
        mParticleDatabaseManager = Mockito.mock(MParticleDBManager::class.java)
        handler = object : MessageHandler(
            mMessageManager,
            MockContext(),
            mParticleDatabaseManager,
            "dataplan1",
            1
        ) {
            public override fun databaseAvailable(): Boolean {
                return true
            }
        }
    }

    @Test
    @Throws(JSONException::class)
    fun testInsertAliasRequest() {
        val insertedAliasRequest = AndroidUtils.Mutable<JSONObject?>(null)
        Mockito.`when`(mConfigManager?.deviceApplicationStamp).thenReturn("das")
        val database: MParticleDBManager = object : MParticleDBManager(MockContext()) {
            override fun insertAliasRequest(apiKey: String, request: JSONObject?) {
                insertedAliasRequest.value = request
            }

            override fun getDatabase(): MPDatabase? {
                return null
            }
        }
        handler?.mMParticleDBManager = database

        TestCase.assertNull(insertedAliasRequest.value)

        val aliasRequest = TestingUtils.getInstance().randomAliasRequest
        val aliasMessage = MPAliasMessage(aliasRequest, "das", "apiKey")

        val mockMessage = Mockito.mock(Message::class.java)
        mockMessage.what = MessageHandler.STORE_ALIAS_MESSAGE
        mockMessage.obj = aliasMessage
        handler?.handleMessageImpl(mockMessage)

        TestCase.assertNotNull(insertedAliasRequest.value)

        aliasMessage.remove(MessageKey.REQUEST_ID)
        insertedAliasRequest.value?.remove(MessageKey.REQUEST_ID)
        TestingUtils.assertJsonEqual(aliasMessage, insertedAliasRequest.value)
    }
}