package com.fintamath;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.View;

import java.util.Map;

import kotlin.Pair;
import kotlin.Triple;

public class KeyboardSwitcher {

    private final Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards;
    private KeyboardView currentKeyboard;

    KeyboardSwitcher(Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards, KeyboardView currentKeyboard) {
        this.keyboards = keyboards;
        this.currentKeyboard = currentKeyboard;
    }

    public void switchKeyboard(KeyboardType keyboardType) {
        currentKeyboard.setVisibility(View.GONE);
        currentKeyboard = keyboards.get(keyboardType).getFirst();
        currentKeyboard.setVisibility(View.VISIBLE);
    }
}
