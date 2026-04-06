package dev.gigaherz.enderrift.automation;

import com.google.common.collect.Lists;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public class EnergyAggregator implements EnergyHandler
{
    private final List<EnergyHandler> buffers = Lists.newArrayList();

    @Override
    public int insert(int maxReceive, TransactionContext transaction)
    {
        int inserted = 0;
        for (int i = 0; i < buffers.size() && inserted < maxReceive; i++)
        {
            var st = buffers.get(i);
            inserted += st.insert(maxReceive - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(int maxExtract, TransactionContext transaction)
    {
        int extracted = 0;
        for (int i = 0; i < buffers.size() && extracted < maxExtract; i++)
        {
            var st = buffers.get(i);
            extracted += st.extract(maxExtract - extracted, transaction);
        }
        return extracted;
    }

    @Override
    public long getAmountAsLong()
    {
        long total = 0;
        for (var st : buffers)
        {
            total = saturateAdd(total, st.getAmountAsLong());
        }
        return total;
    }

    @Override
    public long getCapacityAsLong()
    {
        long total = 0;
        for (var st : buffers)
        {
            total = saturateAdd(total, st.getCapacityAsLong());
        }
        return total;
    }

    private long saturateAdd(long a, long b)
    {
        long c = a+b;
        if (a > 0 && b > 0 && c < 0)
            return Long.MAX_VALUE;
        if (a < 0 && b < 0 && c >= 0)
            return Long.MIN_VALUE;
        return c;
    }

    public void add(EnergyHandler buffer)
    {
        buffers.add(buffer);
    }
}