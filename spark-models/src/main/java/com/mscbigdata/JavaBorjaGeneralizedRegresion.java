package com.mscbigdata;

import org.apache.spark.sql.SparkSession;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;

import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.*;
import static org.apache.spark.sql.types.DataTypes.*;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.functions.*;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.feature.PolynomialExpansion;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.mllib.evaluation.RegressionMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.*;

import org.apache.spark.ml.regression.GeneralizedLinearRegression;
import org.apache.spark.ml.regression.GeneralizedLinearRegressionModel;
import org.apache.spark.ml.regression.GeneralizedLinearRegressionTrainingSummary;

import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;


/**
 * Hello world!
 *
 */
public final class JavaBorjaGeneralizedRegresion{

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      System.err.println("Usage: JavaBorjaLinearRegresion <id>");
      System.exit(1);
    }

  SparkSession spark = SparkSession
      .builder()
      .appName("JavaBorjaLinearRegresion")
      .getOrCreate();

	Dataset<Row> json = spark.read()
    .json("/home/borja/Proyectos/Aplicaciones/spark-2.4.5-bin-hadoop2.7/first-program/src/tweet-1230077874998325248.json");

	//json.printSchema();
  //json.show();

  json.createOrReplaceTempView("json");
  //Dataset<Row> namesDF = spark.sql("SELECT date, retweet FROM json ORDER BY date asc");
  long minimum = (long)spark.sql("SELECT min(date) FROM json").first().get(0);
  System.out.println("minimum: "+minimum);
  long maximum = (long)spark.sql("SELECT max(date) FROM json").first().get(0);
  System.out.println("maximum: "+maximum);

  float fortest = (float)(maximum-minimum)*0.8f;

  //Dataset<Row> datesDF = spark.sql("SELECT date - "+minimum+" as date, retweet, row_number() over (partition by date order by date DESC) FROM json ORDER BY date desc");
  Dataset<Row> test = spark.sql("SELECT date - "+minimum+" as date, retweet FROM json WHERE date > "+(fortest+minimum)+" ORDER BY date desc" );
  test.show(100);
  Dataset<Row> datesDF = spark.sql("SELECT date - "+minimum+" as date, retweet FROM json WHERE date < "+(fortest+minimum)+" ORDER BY date desc" );
  datesDF.printSchema();
  datesDF.show(100);
  System.out.println("count test: "+test.count()+" count datesDF: "+datesDF.count());
  System.out.println("minimum: "+minimum);
  System.out.println("fortest: "+fortest);

  final VectorAssembler vectorAssembler = new VectorAssembler()
        .setInputCols(
            new String[] { "date" }).setOutputCol("features");
  final Dataset<Row> featuresData = vectorAssembler
        .transform(datesDF);
  //featuresData.printSchema();

  final VectorAssembler vectorAssembler2 = new VectorAssembler()
        .setInputCols(
            new String[] { "date" }).setOutputCol("features");
  final Dataset<Row> featuresTest = vectorAssembler
        .transform(test);
  //featuresData.printSchema();

    GeneralizedLinearRegression glr = new GeneralizedLinearRegression()
    .setFamily("poisson")
    .setLink("log")
    .setMaxIter(10)
    .setRegParam(0.3)
    .setFeaturesCol("features")
    .setLabelCol("retweet");

    // Fit the model
    GeneralizedLinearRegressionModel glrmodel = glr.fit(featuresData);
    System.out.println("GeneralizedLinearRegressionModel Coefficients: " + glrmodel.coefficients() + " Intercept: " + glrmodel.intercept());

    Dataset<Row> predictionsGLR = glrmodel.transform(featuresTest);

    System.out.println("evaluator");
    RegressionEvaluator evaluator  =  new  RegressionEvaluator()
      .setLabelCol("date")
      .setPredictionCol("retweet")
      .setMetricName("rmse");

      GeneralizedLinearRegressionTrainingSummary summary = glrmodel.summary();
         System.out.println("Coefficient Standard Errors: "
           + Arrays.toString(summary.coefficientStandardErrors()));
      /*
         System.out.println("T Values: " + Arrays.toString(summary.tValues()));
         System.out.println("P Values: " + Arrays.toString(summary.pValues()));
         System.out.println("Dispersion: " + summary.dispersion());
         System.out.println("Null Deviance: " + summary.nullDeviance());
         System.out.println("Residual Degree Of Freedom Null: " + summary.residualDegreeOfFreedomNull());
         System.out.println("Deviance: " + summary.deviance());
         System.out.println("Residual Degree Of Freedom: " + summary.residualDegreeOfFreedom());
         System.out.println("AIC: " + summary.aic());
         System.out.println("Deviance Residuals: ");
         summary.residuals().show();
    */
    //predictions
    predictionsGLR.show(100);
    predictionsGLR.select("prediction","retweet").show(100);
    RegressionMetrics rmGLR = new RegressionMetrics(predictionsGLR.select("retweet","prediction"));
    System.out.println("RegressionMetrics GLR");
    System.out.println("RegressionMetrics GLR test meanabsolute error: "+(double)rmGLR.meanAbsoluteError());
    System.out.println("RegressionMetrics GLR test RMSE: "+(double)rmGLR.rootMeanSquaredError());
    System.out.println("RegressionMetrics GLR test r2: " + (double)rmGLR.r2());
    double rmse = evaluator.evaluate(predictionsGLR);
    System.out.println("RegressionEvaluator GLR Root mean square error = " + rmse);


    //section: linear regresion model
    LinearRegression lr = new LinearRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)
      .setFeaturesCol("features")
      .setLabelCol("retweet");

    // Fit the model.
    LinearRegressionModel lrModel = lr.fit( featuresData );
    System.out.println("LinearRegressionModel Coefficients: " + lrModel.coefficients() + " Intercept: " + lrModel.intercept());
    LinearRegressionTrainingSummary trainingSummary = lrModel.summary();
    System.out.println("LinearRegressionModel RMSE: " + trainingSummary.rootMeanSquaredError());
    System.out.println("LinearRegressionModel r2: " + trainingSummary.r2());

    // prediction set.
    Dataset<Row> predictionLR = lrModel.transform(featuresTest);
    predictionLR.show(100);
    predictionLR.select("prediction","retweet").show(100);
    RegressionMetrics rm = new RegressionMetrics(predictionLR.select("retweet","prediction"));
    System.out.println("RegressionMetrics");
    System.out.println("RegressionMetrics test meanabsolute error: "+(double)rm.meanAbsoluteError());
    System.out.println("RegressionMetrics test RMSE: "+(double)rm.rootMeanSquaredError());
    System.out.println("RegressionMetrics test r2: " + (double)rm.r2());


    Dataset<Row> predictionPR = glrmodel.transform(featuresTest);
    //predictionPR.show(100);

  spark.stop();
  }
}
