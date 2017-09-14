package com.gx.me.demo4.utils;

import android.util.SparseArray;

import com.gx.me.demo4.model.BaseEvent;
import com.gx.me.demo4.model.IHandler;


public class HandlerMapping {
    public HandlerMapping(SparseArray<IHandler> funcs) {
        this.m_functions = funcs;
    }

    public void addFunc(int what, IHandler handler) {
        m_functions.append(what, handler);
    }

    public int exeFunc(BaseEvent event) {
        IHandler iHandler = m_functions.get(event.what);
        if (iHandler != null) {
            return iHandler.handleFunc(event);
        } else {
            return -1;
        }
    }

    SparseArray<IHandler> m_functions;
}