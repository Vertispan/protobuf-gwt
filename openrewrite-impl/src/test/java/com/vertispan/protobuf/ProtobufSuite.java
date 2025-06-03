package com.vertispan.protobuf;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;

@GwtIncompatible
public class ProtobufSuite {
    public static Test suite() {
        GWTTestSuite suite = new GWTTestSuite("Protobuf emulation tests");
        suite.addTestSuite(MessageTest.class);
        suite.addTestSuite(CodedInputStreamTest.class);

        return suite;
    }
}
