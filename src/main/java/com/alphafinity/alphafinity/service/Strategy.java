package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Strategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(Strategy.class);

    public abstract Context execute(Context context, TimeSeriesEntry data);
    public abstract String strategyName();

}
