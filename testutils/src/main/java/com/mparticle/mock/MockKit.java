package com.mparticle.mock;

import android.content.Context;

import com.mparticle.kits.KitIntegration;
import com.mparticle.kits.ReportingMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MockKit extends KitIntegration {
    @Override
    public String getName() {
        return "Mock Kit";
    }

    @Nullable
    @Override
    public List<ReportingMessage> onKitCreate(@NotNull HashMap<String, String> settings, @Nullable Context context) {return null;}

    @Override
    public List<ReportingMessage> setOptOut(boolean optedOut) {
        return null;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
