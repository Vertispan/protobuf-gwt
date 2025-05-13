package com.example.gwt;

import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;

public class ProtobufForGwt extends Recipe {
    @NlsRewrite.DisplayName
    @Override
    public String getDisplayName() {
        return "Protobuf for GWT";
    }

    @NlsRewrite.Description
    @Override
    public String getDescription() {
        return "Rewrite protobuf classes to be safe for GWT";
    }
}
