package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.Context;
import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;
import com.alphafinity.alphafinity.model.Transaction;
import com.alphafinity.alphafinity.model.enumerations.TransactionOperation;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnalyticsService {

    private static final Double BENCHMARK_RETURN = 0.08; // Assuming the benchmark returns 8% per year
    private static final Double RISK_FREE_RATE = 0.01; // Assuming a risk-free rate of 1%

    public double calculateTotalReturn(Context context) {
        return (context.account.currentCapital - context.account.initialCapital);
    }

    public double calculateTotalReturnMultiplier(Context context) {
        return (context.account.currentCapital - context.account.initialCapital) / context.account.initialCapital;
    }

    public double calculateTotalReturnAsPercentage(Context context) {
        return (context.account.currentCapital - context.account.initialCapital) / context.account.initialCapital * 100;
    }

    // TODO THE VALUE RETURNED IS INCORRECT
    public double calculateSharpeRatio(Context context) {
        List<Double> equityCurve = getEquityCurve(context);

        // Calculate daily returns
        List<Double> returns = IntStream.range(1, equityCurve.size())
                .mapToObj(i -> (equityCurve.get(i) / equityCurve.get(i - 1)) - 1)
                .collect(Collectors.toList());

        double averageReturn = returns.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double excessReturn = averageReturn - RISK_FREE_RATE;

        double standardDeviation = calculateStandardDeviation(returns);

        return excessReturn / standardDeviation;
    }

    public double calculateStandardDeviation(List<Double> returns) {
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0);
        return Math.sqrt(variance);
    }

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

    // Calculates the average profit made / lost per trade
    public double calculateAverageReturnPerTrade(Context context) {
        List<Transaction> closedTrades = context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.CLOSE))
                .toList();

        if (closedTrades.isEmpty()) {
            return 0.0;
        }

        double totalProfit = closedTrades.stream()
                .mapToDouble(transaction -> transaction.profit)
                .sum();

        return totalProfit / closedTrades.size();
    }

    // Calculates the average profit made / lost per winning trade
    public double calculateAverageProfitPerTrade(Context context) {
        List<Transaction> closedTrades = context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.CLOSE))
                .toList();

        if (closedTrades.isEmpty()) {
            return 0.0;
        }

        double totalProfit = closedTrades.stream()
                .mapToDouble(transaction -> transaction.profit) // Assuming getReturn() returns the profit/loss of the trade
                .filter(returnValue -> returnValue > 0)
                .sum();

        long numberOfWinningTrades = closedTrades.stream()
                .filter(transaction -> transaction.profit > 0)
                .count();

        return numberOfWinningTrades > 0 ? totalProfit / numberOfWinningTrades : 0.0;
    }

    // Calculates the average profit made / lost per loosing trade
    public double calculateAverageLossPerTrade(Context context) {
        List<Transaction> closedTrades = context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.CLOSE))
                .toList();

        if (closedTrades.isEmpty()) {
            return 0.0;
        }

        double totalLoss = closedTrades.stream()
                .mapToDouble(transaction -> transaction.profit) // Assuming getReturn() returns the profit/loss of the trade
                .filter(returnValue -> returnValue < 0)
                .sum();

        long numberOfLosingTrades = closedTrades.stream()
                .filter(transaction -> transaction.profit < 0)
                .count();

        return numberOfLosingTrades > 0 ? totalLoss / numberOfLosingTrades : 0.0;
    }

    // Calculate total closed trades
    public Integer calculateTotalClosedTrades(Context context) {
        return Math.toIntExact(context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.CLOSE))
                .count());
    }

    // Calculate total open trades
    public Integer calculateTotalOpenTrades(Context context) {
        return Math.toIntExact(context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.OPEN))
                .count());
    }

    // Calculate total trades (open and closed)
    public Integer calculateTotalTrades(Context context) {
        return context.analytics.transactions.size();
    }

    // Calculate the win rate as a percentage
    public double calculateWinRate(Context context) {
        long totalClosedTrades = context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.CLOSE))
                .count();

        if (totalClosedTrades == 0) {
            return 0.0;
        }

        long winningTrades = context.analytics.transactions.stream()
                .filter(transaction -> transaction.operation.equals(TransactionOperation.CLOSE))
                .filter(transaction -> transaction.profit > 0)
                .count();

        return ((double) winningTrades / totalClosedTrades) * 100;
    }

    public double calculateAlpha(Context context, TimeSeriesData benchmarkTimeSeriesData) {
        double startingCapital = context.account.initialCapital;
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
}
