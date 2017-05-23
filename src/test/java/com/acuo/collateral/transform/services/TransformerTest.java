package com.acuo.collateral.transform.services;

import com.acuo.collateral.transform.Transformer;
import com.acuo.collateral.transform.TransformerContext;
import com.acuo.collateral.transform.margin.DisputeTransformer;
import com.acuo.collateral.transform.margin.MarginSphereTransformer;
import com.acuo.collateral.transform.margin.StatementItemTransformer;
import com.acuo.collateral.transform.trace.transformer_valuations.Mapper;
import com.acuo.common.model.assets.Assets;
import com.acuo.common.model.margin.MarginCall;
import com.acuo.common.model.product.SwapHelper;
import com.acuo.common.model.results.AssetValuation;
import com.acuo.common.model.trade.SwapTrade;
import com.acuo.common.util.ResourceFile;
import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.currency.Currency;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TransformerTest {

    private TransformerContext context = null;

    @Rule
    public ResourceFile received = new ResourceFile("/call/Received_Call.xml");

    @Rule
    public ResourceFile responsjson = new ResourceFile("/reuters/response.json");

    @Rule
    public ResourceFile statementItem = new ResourceFile("/mockmc.csv");

    @Rule
    public ResourceFile portfolioFile = new ResourceFile("/portfolio/TradePortfolio18-05-17v2.xlsx");

    @Before
    public void setup() {
        context = new TransformerContext();
        context.setValueDate(LocalDate.now());
    }

    @Test
    public void testSerialiseSwapsWithClarus() throws Exception {
        Transformer<SwapTrade> transformer = new ClarusTransformer(new Mapper());

        SwapTrade trade = SwapHelper.createTrade();

        String csv = transformer.serialise(ImmutableList.of(trade), context);

        assertThat(csv).isNotEmpty();
    }

    @Test
    public void testSerialiseSwapsWithMarkit() throws Exception {
        Transformer<SwapTrade> transformer = new MarkitTransformer(new Mapper());

        SwapTrade trade = SwapHelper.createTrade();

        String xml = transformer.serialise(ImmutableList.of(trade), context);

        assertThat(xml).isNotEmpty();
    }

    @Test
    public void testSerialiseAssetsWithReuters() throws Exception {
        Transformer<Assets> transformer = new ReutersTransformer<>();

        Assets assets = new Assets();
        assets.setAssetId("1231");
        assets.setAvailableQuantities(11);
        assets.setCurrency(Currency.USD);
        assets.setFitchRating("1");

        String json = transformer.serialise(ImmutableList.of(assets), context);

        assertThat(json).isNotEmpty();
    }

    @Test
    public void testSerialiseAssetsFromReuters() throws Exception {
        Transformer<AssetValuation> transformer = new ReutersTransformer<>();

        List<AssetValuation> assetsList = transformer.deserialiseToList(responsjson.getContent());

        Assert.assertTrue(assetsList.size() > 0);
        AssetValuation valuation = assetsList.get(0);
    }

    @Test
    public void testSerialiseSwapsWithMarginsphere() throws Exception {
        Transformer<MarginCall> transformer = new MarginSphereTransformer(new com.acuo.collateral.transform.trace.transformer_margin.MarginCall());

        List<MarginCall> marginCalls = transformer.deserialiseToList(received.getContent());

        assertThat(marginCalls).isNotEmpty();
    }

    @Test
    public void testStatementItemImport() throws Exception
    {
        Transformer<MarginCall> transformer = new StatementItemTransformer<>(new com.acuo.collateral.transform.trace.transformer_margin.MarginCall());
        List<MarginCall> marginCalls = transformer.deserialiseToList(statementItem.getContent());
        log.info(marginCalls.toString());
        assertThat(marginCalls).isNotEmpty();


    }

    @Test
    public void testDisputeRequest() throws Exception
    {
        DisputeTransformer<MarginCall> transformer = new DisputeTransformer<>(new com.acuo.collateral.transform.trace.transformer_margin.MarginCall());
        MarginCall marginCall = new MarginCall();
        marginCall.setId("cantortest");
        List<MarginCall> marginCalls = new ArrayList<>();
        marginCalls.add(marginCall);
        String reqeset = transformer.serialise(marginCalls, null );
        assertThat(reqeset).isNotEmpty();
    }


    @Test
    public void testPortfolio() throws Exception
    {
        Transformer<SwapTrade> transformer = new PortfolioImportTransformer(new Mapper());
        transformer.deserialise(toByteArray(portfolioFile.getInputStream()));
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}