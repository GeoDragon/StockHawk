package com.udacityprojects.geonarz.stockhawk.Widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class QuoteWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuoteViewsFactory(this);
    }
}