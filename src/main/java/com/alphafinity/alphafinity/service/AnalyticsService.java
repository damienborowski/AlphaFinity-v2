package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.Context;
import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;
import com.alphafinity.alphafinity.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnalyticsService {

    private static final Double BENCHMARK_RETURN = 0.08; // Assuming the benchmark returns 8% per year

    public double calculateTotalReturn(Context context) {
        return (context.account.currentCapital - context.account.startingCapital);
    }

    public double calculateTotalReturnMultiplier(Context context) {
        return (context.account.currentCapital - context.account.startingCapital) / context.account.startingCapital;
    }

    public double calculateTotalReturnAsPercentage(Context context) {
        return (context.account.currentCapital - context.account.startingCapital) / context.account.startingCapital * 100;
    }


//    public double calculateSharpeRatio(Context context) {
//        double riskFreeRate = 0.01; // Assuming 1% risk-free rate
//        double excessReturn = calculateTotalReturn(context) - riskFreeRate;
//        double standardDeviation = calculateStandardDeviation(context.analytics.getReturns());
//        return excessReturn / standardDeviation;
//    }

    public double calculateMaxDrawdown(Context context) {
        List<Double> equityCurve = getEquityCurve(context);

        return IntStream.range(0, equityCurve.size())
                .mapToDouble(i -> {
                    double peak = equityCurve.subList(0, i + 1).stream().max(Double::compare).orElse(0.0);
                    return (peak - equityCurve.get(i)) / peak;
                })
                .max()
                .orElse(0.0);
    }

    public List<Double> getEquityCurve(Context context) {
        return IntStream.range(0, context.getClosedTransactions().size())
                .mapToObj(i -> context.getClosedTransactions().subList(0, i + 1)
                        .stream()
                        .mapToDouble(transaction -> transaction.profit)
                        .sum())
                .collect(Collectors.toList());
    }

//    public double calculateWinRate(Context context) {
//        long wins = context.analytics.getTrades().stream().filter(trade -> trade.getReturn() > 0).count();
//        return (double) wins / context.analytics.getTrades().size();
//    }

//    public double calculateAverageWin(Context context) {
//        return context.getClosedTransactions().stream()
//                .filter(transaction -> )
//                .mapToDouble(TradeExecutionResponse::getReturn)
//                .average()
//                .orElse(0);
//    }

//    public double calculateAverageLoss(Context context) {
//        return context.analytics.transactions.stream()
//                .filter(trade -> trade.getReturn() < 0)
//                .mapToDouble(TradeExecutionResponse::getReturn)
//                .average()
//                .orElse(0);
//    }

    // todo this is incorrect. We need to add filter based on either open trades, closed trades or whatever other metric
    public int calculateTotalTrades(Context context) {
        return context.analytics.transactions.size();
    }

    public double calculateAlpha(Context context, TimeSeriesData benchmarkTimeSeriesData) {
        double startingCapital = context.account.startingCapital;
        double endingCapital = context.account.currentCapital;

        // Calculate the total period in years TODO FIX LOCAL_DATE_TIME
        LocalDateTime startDate = context.analytics.startDate.atStartOfDay();
        LocalDateTime endDate = context.analytics.endDate.atStartOfDay();

        long daysBetween = Duration.between(startDate, endDate).toDays();
        double years = daysBetween / 365.25;

        double strategyCAGR = calculateCAGR(startingCapital, endingCapital, years);
        double benchmarkCAGR = calculateBenchmarkCAGR(benchmarkTimeSeriesData);

        return strategyCAGR - benchmarkCAGR;
    }

    private double calculateCAGR(double beginningValue, double endingValue, double years) {
        // (Compounded Annual Growth Rate)
        return Math.pow((endingValue / beginningValue), (1.0 / years)) - 1;
    }

    private double calculateBenchmarkCAGR(TimeSeriesData benchmarkData) {
        List<TimeSeriesEntry> entries = benchmarkData.entries;

        double startingValue = entries.get(0).close;
        double endingValue = entries.get(entries.size() - 1).close;

        LocalDateTime startDate = entries.get(0).datetime.atStartOfDay();
        LocalDateTime endDate = entries.get(entries.size() - 1).datetime.atStartOfDay();

        long daysBetween = Duration.between(startDate, endDate).toDays();
        double years = daysBetween / 365.25;

        return calculateCAGR(startingValue, endingValue, years);
    }

    public double calculateStandardDeviation(List<Double> returns) {
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }
}
