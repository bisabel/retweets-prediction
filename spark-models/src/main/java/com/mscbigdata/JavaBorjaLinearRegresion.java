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

import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.feature.PolynomialExpansion;

import org.apache.spark.ml.regression.GeneralizedLinearRegression;
import org.apache.spark.ml.regression.GeneralizedLinearRegressionModel;
import org.apache.spark.ml.regression.GeneralizedLinearRegressionTrainingSummary;

import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;


/**
 * Hello world!
 *
 */
public final class JavaBorjaLinearRegresion{
  //private static final Pattern SPACE = Pattern.compile(" ");

  public static class myPoint implements Serializable {
        private double retweet;
        private int like;
        private int comments;
        //private java.sql.Timestamp date;
        private int date;//timestand convert to seconds

        public myPoint(double retweet, int like, int date) {
            System.out.println("myPoint: "+date);
            this.retweet = retweet;
            this.like = like;
            this.date = date;
        }

        public double getRetweet() {
            return retweet;
        }

        public void setRetweet(double retweet) {
            this.retweet = retweet;
        }

        public int getLike() {
            return like;
        }

        public void setLike(int like) {
            this.like = like;
        }

        public int getComments() {
            return comments;
        }

        public void setComments(int comments) {
            this.comments = comments;
        }
/*
        public java.sql.Timestamp getDate() {
            return date;
        }

        public void setDate(java.sql.Timestamp date) {
            this.date = date;
        }
*/
        public int getDate() {
            return date;
        }

        public void setDate(int date) {
            this.date = date;
        }

    }

  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      System.err.println("Usage: JavaBorjaLinearRegresion <id>");
      System.exit(1);
    }

    SparkSession spark = SparkSession
      .builder()
      .appName("JavaBorjaLinearRegresion")
      .getOrCreate();

    System.out.println("mypointEncoder");
    Encoder<myPoint> mypointEncoder = Encoders.bean(myPoint.class);
	/*
	List<myPoint> data = Arrays.asList(
			new myPoint(1.0,1.0,"Mon Dec 30 16:48:26 +0000 2019"),
			new myPoint(4.0,3.0,"Mon Dec 30 18:48:26 +0000 2019"));
	*/
	//Dataset<myPoint> ds = spark.createDataset(data, mypointEncoder);
    //ds.show();
	//linear
	//Dataset<Row> training = spark.read().format("libsvm")
	//  .load("data/mllib/sample_linear_regression_data.txt");
  StructType schema = new StructType(new StructField[]{
    new StructField("retweet", DoubleType, false, Metadata.empty()),
    new StructField("like", IntegerType, false, Metadata.empty()),
    new StructField("comments", IntegerType, false, Metadata.empty()),
    //new StructField("date", TimestampType, false, Metadata.empty())
    new StructField("date", IntegerType, false, Metadata.empty()),
    //
  });

	Dataset<myPoint> json = spark.read()
    .schema(schema)
    .json("/home/borja/Proyectos/Aplicaciones/spark-2.4.5-bin-hadoop2.7/first-program/src/tweet-hallstatt.json")
    .as(mypointEncoder);

  Dataset<Row> json2 = json.withColumn("date2", functions.pow(json.col("date"),2));

	json2.printSchema();
  json2.show();

	System.out.println("createDataFrame");

	LinearRegression lr = new LinearRegression()
	  .setMaxIter(10)
	  .setRegParam(0.3)
	  .setElasticNetParam(0.8)
	  .setFeaturesCol("features")
	  .setLabelCol("retweet");

	final VectorAssembler vectorAssembler = new VectorAssembler()
			.setInputCols(
					new String[] { "date", "date2" }).setOutputCol("features");
	final Dataset<Row> featuresData = vectorAssembler
			.transform(json2);
	featuresData.printSchema();

	// Fit the model.
	LinearRegressionModel lrModel = lr.fit( featuresData );

	// Print the coefficients and intercept for linear regression.
	System.out.println("LinearRegressionModel Coefficients: " + lrModel.coefficients() + " Intercept: " + lrModel.intercept());

	// Summarize the model over the training set and print out some metrics.
	/*
	LinearRegressionTrainingSummary trainingSummary = lrModel.summary();
	System.out.println("numIterations: " + trainingSummary.totalIterations());
	System.out.println("objectiveHistory: " + Vectors.dense(trainingSummary.objectiveHistory()));
	trainingSummary.residuals().show();
	System.out.println("RMSE: " + trainingSummary.rootMeanSquaredError());
	System.out.println("r2: " + trainingSummary.r2());

  final VectorAssembler vectorAssembler2 = new VectorAssembler()
      .setInputCols(
          new String[] { "date", "retweet" }).setOutputCol("features");
  final Dataset<Row> featuresData2 = vectorAssembler2
      .transform(json2);
  PolynomialExpansion polyExpansion = new PolynomialExpansion()
    .setInputCol("features")
    .setOutputCol("polynomialFeature")
    .setDegree(2);

  Dataset<Row> polyDF = polyExpansion.transform(featuresData2);
  System.out.println("polyExpansion Coefficients: " + polyDF.coefficients() + " Intercept: " + polyDF.intercept());
  polyDF.show(false);
  */

  final VectorAssembler vectorAssembler2 = new VectorAssembler()
      .setInputCols(
          new String[] { "date" }).setOutputCol("features");
  final Dataset<Row> featuresData2 = vectorAssembler2
      .transform(json2);
  featuresData2.printSchema();

  GeneralizedLinearRegression glr = new GeneralizedLinearRegression()
  .setFamily("poisson")
  .setLink("log")
  .setMaxIter(10)
  .setRegParam(0.3)
  .setFeaturesCol("features")
  .setLabelCol("retweet");

  // Fit the model
  GeneralizedLinearRegressionModel glrmodel = glr.fit(featuresData2);
  System.out.println("GeneralizedLinearRegressionModel Coefficients: " + glrmodel.coefficients() + " Intercept: " + glrmodel.intercept());


  spark.stop();
  }
}
