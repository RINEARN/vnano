/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.HashMap;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;

/**
 * <p>
 * Vnano処理系内におけるデータ単位である {@link DataContainer DataContainer} と、
 * ホスト言語側におけるデータを相互変換するクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class DataConverter {


	/**
	 * {@link DataConverter DataConverter} 内でのデータ型変換処理のため、
	 * ホスト言語側の型（プリミティブ型やクラス型）を分類した列挙子です。
	 */
	private enum ExternalType {

		/** 32ビット精度の符号付き整数型（int）です。 */
		INT32,

		/** 64ビット精度の符号付き整数型（long）です。 */
		INT64,

		/** 32ビット精度の符号付き浮動小数点数型（float）です。 */
		FLOAT32,

		/** 64ビット精度の符号付き浮動小数点数型（double）です。 */
		FLOAT64,

		/** 文字列型（String）です。 */
		STRING,

		/** 論理型（boolean）です。 */
		BOOL,

		/** void型です。 */
		VOID;
	}


	/** ホスト言語における、32ビット精度の符号付き整数のプリミティブ型の名称です。 */
	private static final String EXTERNAL_TYPE_NAME_INT32 = "int";

	/** ホスト言語における、64ビット精度の符号付き整数のプリミティブ型の名称です。 */
	private static final String EXTERNAL_TYPE_NAME_INT64 = "long";

	/** ホスト言語における、32ビット精度の符号付き浮動小数点数のプリミティブ型の名称です。 */
	private static final String EXTERNAL_TYPE_NAME_FLOAT32 = "float";

	/** ホスト言語における、64ビット精度の符号付き浮動小数点数のプリミティブ型の名称です。 */
	private static final String EXTERNAL_TYPE_NAME_FLOAT64 = "double";

	/** ホスト言語における、論理型のプリミティブ型の名称です。 */
	private static final String EXTERNAL_TYPE_NAME_BOOL = "boolean";

	/** ホスト言語における、文字列型の名称です。 */
	private static final String EXTERNAL_TYPE_NAME_STRING = "java.lang.String";

	/** ホスト言語における、void型の名称（void）です。 */
	private static final String EXTERNAL_TYPE_NAME_VOID = "void";

	/** ホスト言語における、32ビット精度の符号付き整数型のラッパークラスの名称です。 */
	private static final String EXTERNAL_TYPE_NAME_INT32_WRAPPER = "java.lang.Integer";

	/** ホスト言語における、64ビット精度の符号付き整数型のラッパークラスの名称です。 */
	private static final String EXTERNAL_TYPE_NAME_INT64_WRAPPER = "java.lang.Long";

	/** ホスト言語における、32ビット精度の符号付き浮動小数点数型のラッパークラスの名称です。 */
	private static final String EXTERNAL_TYPE_NAME_FLOAT32_WRAPPER = "java.lang.Float";

	/** ホスト言語における、64ビット精度の符号付き浮動小数点数型のラッパークラスの名称です。 */
	private static final String EXTERNAL_TYPE_NAME_FLOAT64_WRAPPER = "java.lang.Double";

	/** ホスト言語における、論理型のラッパークラスの名称（Boolean）です。 */
	private static final String EXTERNAL_TYPE_NAME_BOOL_WRAPPER = "java.lang.Boolean";

	/** ホスト言語における、void型のラッパー（プレースホルダ）クラスの名称です。 */
	private static final String EXTERNAL_TYPE_NAME_VOID_WRAPPER = "java.lang.Void";

	/** ホスト言語における、配列型のブラケット部の表記です。 */
	private static final String EXTERNAL_ARRAY_BRACKET = "[]";

	/** ホスト言語における、配列型のブラケット部の正規表現です。 */
	private static final String EXTERNAL_ARRAY_BRACKET_REGEX = "\\[\\]";


	/**
	 * ホスト言語のデータ型名（クラス名またはプリミティブ名）をキーとし、
	 * それをVnano処理系内でのデータ型を分類した
	 * {@link DataType DataType} 列挙子の要素に変換するためのマップです。
	 */
	private static final HashMap<String,DataType> EXTERNAL_NAME_DATA_TYPE_MAP = new HashMap<String,DataType>();
	static {
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_STRING, DataType.STRING);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL, DataType.BOOL);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID, DataType.VOID);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32_WRAPPER, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64_WRAPPER, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32_WRAPPER, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64_WRAPPER, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL_WRAPPER, DataType.BOOL);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID_WRAPPER, DataType.VOID);
	}


	/**
	 * ホスト言語のデータ型名（クラス名またはプリミティブ名）をキーとし、
	 * それをホスト言語のデータ型（クラス）を分類した
	 * {@link org.vcssl.nano.interconnect.DataConverter.ExternalType} 列挙子の要素に変換するためのマップです。
	 */
	private static final HashMap<String,ExternalType> EXTERNAL_NAME_EXTERNAL_TYPE_MAP = new HashMap<String,ExternalType>();
	static {
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32, ExternalType.INT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64, ExternalType.INT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32, ExternalType.FLOAT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64, ExternalType.FLOAT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_STRING, ExternalType.STRING);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL, ExternalType.BOOL);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID, ExternalType.VOID);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32_WRAPPER, ExternalType.INT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64_WRAPPER, ExternalType.INT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32_WRAPPER, ExternalType.FLOAT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64_WRAPPER, ExternalType.FLOAT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL_WRAPPER, ExternalType.BOOL);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID_WRAPPER, ExternalType.VOID);
	}


	/**
	 * ホスト言語のデータ型（クラス）を分類した
	 * {@link org.vcssl.nano.interconnect.DataConverter.ExternalType} 列挙子の要素をキーとし、
	 * それをVnano処理系内でのデータ型を分類した
	 * {@link DataType DataType} 列挙子の要素に変換するためのマップです。
	 */
	private static final HashMap<ExternalType, DataType> EXTERNAL_TYPE_DATA_TYPE_MAP = new HashMap<ExternalType, DataType>();
	static {
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.INT64, DataType.INT64);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.FLOAT64, DataType.FLOAT64);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.BOOL, DataType.BOOL);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.STRING, DataType.STRING);
	}


	/**
	 * Vnano処理系内でのデータ型を分類した
	 * {@link DataType DataType} 列挙子の要素をキーとし、
	 * それをホスト言語のデータ型（クラス）を分類した
	 * {@link org.vcssl.nano.interconnect.DataConverter.ExternalType} 列挙子の要素に変換するためのマップです。
	 */
	private static final HashMap<DataType, ExternalType> DATA_TYPE_EXTERNAL_TYPE_MAP = new HashMap<DataType, ExternalType>();
	static {
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.INT64, ExternalType.INT64);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.FLOAT64, ExternalType.FLOAT64);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.BOOL, ExternalType.BOOL);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.STRING, ExternalType.STRING);
	}


	/** インスタンスが行う変換処理における、ホスト言語側のデータ型情報を保持します。 */
	private ExternalType externalType = null;

	/** インスタンスが行う変換処理における、処理系内側のデータ型情報を保持します。 */
	private DataType dataType = null;

	/** インスタンスが行う変換処理における、配列次元数を保持します。 */
	private int rank = -1;


	/**
	 * ホスト言語における Class で表現されたデータ型のデータを、
	 * ホスト言語側と処理系内側で双方向変換可能なデータコンバータを生成します。
	 *
	 * 引数 objectClass には、変換対象とするデータ型における、
	 * ホスト言語側におけるクラスを指定してください。
	 * ただし、変換対象のデータ型が、ホスト言語側でプリミティブ型の場合は、
	 * そのラッパークラスを指定してください。
	 *
	 * @param objectClass ホスト言語側でのデータ型を表すクラス（プリミティブ型の場合はラッパークラス）
	 * @throws VnanoException 未対応のデータ型が指定された場合にスローされます。
	 */
	public DataConverter(Class<?> objectClass) throws VnanoException {
		this.rank = this.getRankOf(objectClass);
		String externalDataTypeName = getExternalTypeNameOf(objectClass);
		this.externalType = EXTERNAL_NAME_EXTERNAL_TYPE_MAP.get(externalDataTypeName);
		this.dataType = EXTERNAL_NAME_DATA_TYPE_MAP.get(externalDataTypeName);

		if (this.dataType == null) {
			VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_DATA_TYPE);
			e.setErrorWords(new String[] {externalType.getClass().getCanonicalName()});
			throw e;
		}
	}


	/**
	 * {@link org.vcssl.nano.lang.DataType DataType}
	 * で指定された処理系内部におけるデータ型と、指定された配列次元数を持つデータを、
	 * ホスト言語側と処理系内側で双方向変換可能なデータコンバータを生成します。
	 *
	 * 引数 objectClass には、変換対象とするデータ型における、
	 * ホスト言語側におけるクラスを指定してください。
	 * ただし、変換対象のデータ型が、ホスト言語側でプリミティブ型の場合は、
	 * そのラッパークラスを指定してください。
	 *
	 * @param objectClass ホスト言語側でのデータ型を表すクラス（プリミティブ型の場合はラッパークラス）
	 * @throws VnanoException 未対応のデータ型が指定された場合にスローされます。
	 */
	public DataConverter(DataType dataType, int rank) throws VnanoException {
		this.rank = rank;
		this.dataType = dataType;
		this.externalType = DATA_TYPE_EXTERNAL_TYPE_MAP.get(dataType);
	}


	/**
	 * 指定されたホスト言語側のクラスのオブジェクトが、処理系内側のデータ型に変換かのうかどうかを判断して返します。
	 *
	 * @param objectClass 変換可能か検査するクラス
	 * @return 変換可能であればtrue
	 */
	public static boolean isConvertible(Class<?> objectClass) {
		String externalDataTypeName = getExternalTypeNameOf(objectClass);
		DataType dataType = EXTERNAL_NAME_DATA_TYPE_MAP.get(externalDataTypeName);
		return dataType != null;
	}


	/**
	 * データ型を表すホスト言語側のクラスから、ホスト言語側における型名を求めて返します。
	 * @param objectClass 対象データ型のクラス
	 * @return 対象データ型のホスト言語側における名称
	 */
	private static final String getExternalTypeNameOf(Class<?> objectClass) {
		String className = objectClass.getCanonicalName();
		String typeName = null;
		boolean isArray = 0 <= className.indexOf(EXTERNAL_ARRAY_BRACKET);
		if (isArray) {
			typeName = className.substring(0, className.indexOf(EXTERNAL_ARRAY_BRACKET));
		} else {
			typeName = className;
		}
		return typeName;
	}


	/**
	 * データ型を表すホスト言語側のクラスから、配列次元数を判定して返します。
	 *
	 * @param objectClass 配列次元数を判定したいクラス
	 * @return クラスの配列次元数
	 */
	private int getRankOf(Class<?> objectClass) {
		String className = objectClass.getCanonicalName();
		int arrayRank = -1;

		// クラス名の文字列表現が "[]" 記号を含んでいれば配列型
		boolean isArray = 0 <= className.indexOf(EXTERNAL_ARRAY_BRACKET);

		if (isArray) {
			// 配列の場合、クラス名が含む "[]" の個数が次元数なので、"[]" で分割して求める
			arrayRank = className.split(EXTERNAL_ARRAY_BRACKET_REGEX, -1).length - 1;
		} else {
			// 非配列なら 0 次元とする
			arrayRank = DataContainer.RANK_OF_SCALAR;
		}
		return arrayRank;
	}

	/**
	 * インスタンスが行うデータ型変換における、Vnano処理系内側でのデータ型を返します。
	 *
	 * @return 変換データ型（処理系内側）
	 */
	public DataType getDataType() {
		return this.dataType;
	}


	/**
	 * インスタンスが行うデータ型変換における、配列次元数を返します。
	 * データがスカラの場合は 0 が返されます。
	 *
	 * @return 配列次元数（スカラの場合は 0 ）
	 */
	public int getRank() {
		return this.rank;
	}


	/**
	 * ホスト言語側におけるデータのオブジェクトを、
	 * Vnano処理系内における適切なデータ型のデータに変換し、
	 * それを保持するデータコンテナを返します。
	 *
	 * @param externalObject ホスト言語側におけるデータのオブジェクト
	 * @return 変換されたデータを保持するデータコンテナ
	 * @throws VnanoException
	 * 		変換対象データが、処理系内で表現できない型を持っていた場合や、
	 * 		配列次元数が変換処理の上限（現在の実装では 3 次元まで）
	 * 		を超えていた場合にスローされます。
	 */
	public DataContainer<?> convertToDataContainer(Object externalObject) throws VnanoException {
		DataContainer<?> internalData = (DataContainer<?>)new DataContainer<Void>();
		this.convertToDataContainer(externalObject, internalData);
		return internalData;
	}


	/**
	 * {@link DataConverter#convertToDataContainer(Object) convertToDataContainer(Object)}
	 * メソッドと同じ処理を行いますが、変換結果を戻り値として返す代わりに、
	 * 第二引数に指定されたデータコンテナに格納します。
	 *
	 * @param object ホスト言語側におけるデータのオブジェクト
	 * @param resultDataContainer 変換されたデータを格納するデータコンテナ
	 * @throws VnanoException
	 * 		変換対象データが、処理系内で表現できない型を持っていた場合や、
	 * 		配列次元数が変換処理の上限（現在の実装では 3 次元まで）を超えていた場合、
	 * 		またはジャグ配列となっていた場合にスローされます。
	 */
	public void convertToDataContainer(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		switch (this.rank) {
			case 0 : {
				this.convertToDataContainer0D(object, resultDataContainer);
				return;
			}
			case 1 : {
				this.convertToDataContainer1D(object, resultDataContainer);
				return;
			}
			case 2 : {
				this.convertToDataContainer2D(object, resultDataContainer);
				return;
			}
			case 3 : {
				this.convertToDataContainer3D(object, resultDataContainer);
				return;
			}
			default : {
				VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_ARRAY);
				e.setErrorWords(new String[] {getExternalTypeNameOf(object.getClass())});
				throw e;
			}
		}
	}


	/**
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * メソッド内の処理において、配列次元数が0（スカラ）の場合の処理を行います。
	 *
	 * @param object ホスト言語側におけるデータのオブジェクト
	 * @param resultDataContainer 変換されたデータを格納するデータコンテナ
	 * @throws VnanoException データの型が void 型であった場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer0D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int dataLength = DataContainer.SIZE_OF_SCALAR;
		int[] arrayLength = DataContainer.LENGTHS_OF_SCALAR;
		resultDataContainer.setSize(dataLength);
		resultDataContainer.setLengths(arrayLength);
		switch (this.externalType) {
			case INT32 : {
				long[] data = new long[]{ ((Integer)object).longValue() };
				((DataContainer<long[]>)resultDataContainer).setData(data);
				return;
			}
			case INT64 : {
				long[] data = new long[]{ ((Long)object).longValue() };
				((DataContainer<long[]>)resultDataContainer).setData(data);
				return;
			}
			case FLOAT32 : {
				double[] data = new double[]{ ((Float)object).doubleValue() };
				((DataContainer<double[]>)resultDataContainer).setData(data);
				return;
			}
			case FLOAT64 : {
				double[] data = new double[]{ ((Double)object).doubleValue() };
				((DataContainer<double[]>)resultDataContainer).setData(data);
				return;
			}
			case BOOL : {
				boolean[] data = new boolean[]{ ((Boolean)object).booleanValue() };
				((DataContainer<boolean[]>)resultDataContainer).setData(data);
				return;
			}
			case STRING : {
				String[] data = new String[]{ (String)object };
				((DataContainer<String[]>)resultDataContainer).setData(data);
				return;
			}
			case VOID : {

				VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_DATA_TYPE);
				e.setErrorWords(new String[] {DataTypeName.getDataTypeNameOf(DataType.VOID)});
				throw e;
			}
		}
	}


	/**
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * メソッド内の処理において、配列次元数が1の場合の処理を行います。
	 *
	 * @param object ホスト言語側におけるデータのオブジェクト
	 * @param resultDataContainer 変換されたデータを格納するデータコンテナ
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer1D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = new int[1];
		int dataLength = -1;
		switch (this.externalType) {
			case INT32 : {
				dataLength = ((int[])object).length;
				arrayLength[0] = dataLength;
				long[] data = new long[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((long[])data)[dataIndex] = ((int[])object)[dataIndex];
				}
				((DataContainer<long[]>)resultDataContainer).setData(data);
				break;
			}
			case INT64 : {
				dataLength = ((long[])object).length;
				arrayLength[0] = dataLength;
				long[] data = new long[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((long[])data)[dataIndex] = ((long[])object)[dataIndex];
				}
				((DataContainer<long[]>)resultDataContainer).setData(data);
				resultDataContainer.setSize(dataLength);
				resultDataContainer.setLengths(arrayLength);
				break;
			}
			case FLOAT32 : {
				dataLength = ((float[])object).length;
				arrayLength[0] = dataLength;
				double[] data = new double[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((double[])data)[dataIndex] = ((float[])object)[dataIndex];
				}
				((DataContainer<double[]>)resultDataContainer).setData(data);
				resultDataContainer.setSize(dataLength);
				resultDataContainer.setLengths(arrayLength);
				break;
			}
			case FLOAT64 : {
				dataLength = ((double[])object).length;
				arrayLength[0] = dataLength;
				double[] data = new double[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((double[])data)[dataIndex] = ((double[])object)[dataIndex];
				}
				((DataContainer<double[]>)resultDataContainer).setData(data);
				resultDataContainer.setSize(dataLength);
				resultDataContainer.setLengths(arrayLength);
				break;
			}
			case BOOL : {
				dataLength = ((boolean[])object).length;
				arrayLength[0] = dataLength;
				boolean[] data = new boolean[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((boolean[])data)[dataIndex] = ((boolean[])object)[dataIndex];
				}
				((DataContainer<boolean[]>)resultDataContainer).setData(data);
				resultDataContainer.setSize(dataLength);
				resultDataContainer.setLengths(arrayLength);
				break;
			}
			case STRING : {
				dataLength = ((String[])object).length;
				arrayLength[0] = dataLength;
				String[] data = new String[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((String[])data)[dataIndex] = ((String[])object)[dataIndex];
				}
				((DataContainer<String[]>)resultDataContainer).setData(data);
				break;
			}
			case VOID : {
				// void型の配列はホスト言語側で存在し得ないため、ここが実行される事は無い
				break;
			}
		}
		resultDataContainer.setSize(dataLength);
		resultDataContainer.setLengths(arrayLength);
	}


	/**
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * メソッド内の処理において、配列次元数が2の場合の処理を行います。
	 *
	 * @param object ホスト言語側におけるデータのオブジェクト
	 * @param resultDataContainer 変換されたデータを格納するデータコンテナ
	 * @throws VnanoException データがジャグ配列であった場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer2D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = new int[2];
		int dataLength = -1;
		switch (this.externalType) {
			case INT32 : {
				arrayLength[0] = ((int[][])object).length;
				arrayLength[1] = ((int[][])object)[0].length;
				dataLength = arrayLength[0] * arrayLength[1];
				long[] data = new long[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((int[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// 変換
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((long[])data)[dataIndex] = ((int[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<long[]>)resultDataContainer).setData(data);
				break;
			}
			case INT64 : {
				arrayLength[0] = ((long[][])object).length;
				arrayLength[1] = ((long[][])object)[0].length;
				dataLength = arrayLength[0] * arrayLength[1];
				long[] data = new long[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((long[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// 変換
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((long[])data)[dataIndex] = ((long[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<long[]>)resultDataContainer).setData(data);
				break;
			}
			case FLOAT32 : {
				arrayLength[0] = ((float[][])object).length;
				arrayLength[1] = ((float[][])object)[0].length;
				dataLength = arrayLength[0] * arrayLength[1];
				double[] data = new double[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((double[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// 変換
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((double[])data)[dataIndex] = ((float[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<double[]>)resultDataContainer).setData(data);
				break;
			}
			case FLOAT64 : {
				arrayLength[0] = ((double[][])object).length;
				arrayLength[1] = ((double[][])object)[0].length;
				dataLength = arrayLength[0] * arrayLength[1];
				double[] data = new double[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((float[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// 変換
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((double[])data)[dataIndex] = ((double[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<double[]>)resultDataContainer).setData(data);
				break;
			}
			case BOOL : {
				arrayLength[0] = ((boolean[][])object).length;
				arrayLength[1] = ((boolean[][])object)[0].length;
				dataLength = arrayLength[0] * arrayLength[1];
				boolean[] data = new boolean[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((boolean[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// 変換
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((boolean[])data)[dataIndex] = ((boolean[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<boolean[]>)resultDataContainer).setData(data);
				break;
			}
			case STRING : {
				arrayLength[0] = ((String[][])object).length;
				arrayLength[1] = ((String[][])object)[0].length;
				dataLength = arrayLength[0] * arrayLength[1];
				String[] data = new String[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((String[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// 変換
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((String[])data)[dataIndex] = ((String[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<String[]>)resultDataContainer).setData(data);
				break;
			}
			case VOID : {
				// void型の配列はホスト言語側で存在し得ないため、ここが実行される事は無い
				break;
			}
		}
		resultDataContainer.setSize(dataLength);
		resultDataContainer.setLengths(arrayLength);
		return;
	}


	/**
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * メソッド内の処理において、配列次元数が3の場合の処理を行います。
	 *
	 * @param object ホスト言語側におけるデータのオブジェクト
	 * @param resultDataContainer 変換されたデータを格納するデータコンテナ
	 * @throws VnanoException データがジャグ配列であった場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer3D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = new int[3];
		int dataLength = -1;
		switch (this.externalType) {
			case INT32 : {
				arrayLength[0] = ((int[][][])object).length;
				arrayLength[1] = ((int[][][])object)[0].length;
				arrayLength[2] = ((int[][][])object)[0][0].length;
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				long[] data = new long[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((int[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// ジャグ配列検査
						if ( ((int[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// 変換
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((long[])data)[dataIndex] = ((int[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<long[]>)resultDataContainer).setData(data);
				break;
			}
			case INT64 : {
				arrayLength[0] = ((long[][][])object).length;
				arrayLength[1] = ((long[][][])object)[0].length;
				arrayLength[2] = ((long[][][])object)[0][0].length;
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				long[] data = new long[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((long[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// ジャグ配列検査
						if ( ((long[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// 変換
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((long[])data)[dataIndex] = ((long[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<long[]>)resultDataContainer).setData(data);
				break;
			}
			case FLOAT32 : {
				arrayLength[0] = ((float[][][])object).length;
				arrayLength[1] = ((float[][][])object)[0].length;
				arrayLength[2] = ((float[][][])object)[0][0].length;
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				double[] data = new double[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((float[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// ジャグ配列検査
						if ( ((float[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// 変換
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((double[])data)[dataIndex] = ((float[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<double[]>)resultDataContainer).setData(data);
				break;
			}
			case FLOAT64 : {
				arrayLength[0] = ((double[][][])object).length;
				arrayLength[1] = ((double[][][])object)[0].length;
				arrayLength[2] = ((double[][][])object)[0][0].length;
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				double[] data = new double[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((float[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// ジャグ配列検査
						if ( ((double[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// 変換
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((double[])data)[dataIndex] = ((double[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<double[]>)resultDataContainer).setData(data);
				break;
			}
			case BOOL : {
				arrayLength[0] = ((boolean[][][])object).length;
				arrayLength[1] = ((boolean[][][])object)[0].length;
				arrayLength[2] = ((boolean[][][])object)[0][0].length;
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				boolean[] data = new boolean[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((boolean[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// ジャグ配列検査
						if ( ((boolean[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// 変換
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((boolean[])data)[dataIndex] = ((boolean[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<boolean[]>)resultDataContainer).setData(data);
				break;
			}
			case STRING : {
				arrayLength[0] = ((String[][][])object).length;
				arrayLength[1] = ((String[][][])object)[0].length;
				arrayLength[2] = ((String[][][])object)[0][0].length;
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				String[] data = new String[ dataLength ];
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// ジャグ配列検査
					if ( ((String[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// ジャグ配列検査
						if ( ((String[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// 変換
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((String[])data)[dataIndex] = ((String[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<String[]>)resultDataContainer).setData(data);
				break;
			}
			case VOID : {
				// void型の配列はホスト言語側で存在し得ないため、ここが実行される事は無い
				break;
			}
		}
		resultDataContainer.setSize(dataLength);
		resultDataContainer.setLengths(arrayLength);
	}



	/**
	 * Vnano処理系内側のデータ型に基づくデータを、
	 * ホスト言語側の適切なデータ型のオブジェクトに変換して返します。
	 * プリミティブ型のデータは、ラッパークラスのインスタンスに格納して返されます。
	 *
	 * @param dataContainer 変換するデータを保持するデータコンテナ
	 * @return ホスト言語側のデータ型に変換されたオブジェクト
	 * @throws VnanoException
	 * 		変換対象データの型が void 型であった場合や、
	 * 		配列次元数が変換処理の上限（現在の実装では 3 次元まで）を超えていた場合にスローされます。
	 */
	public Object convertToExternalObject(DataContainer<?> dataContainer) throws VnanoException {
			//throws InvalidDataTypeException {

		Object internalData = dataContainer.getData();
		int[] arrayLength = dataContainer.getLengths();
		int dataLength = dataContainer.getSize();

		switch (this.rank) {

			case DataContainer.RANK_OF_SCALAR : {
				int dataIndex = dataContainer.getOffset();

				switch (this.externalType) {

					case INT32 : {
						return new Integer( (int)( ((long[])internalData)[dataIndex] ) );
					}
					case INT64 : {
						return new Long( ((long[])internalData)[dataIndex] );
					}
					case FLOAT32 : {
						return new Float( (float)( ((double[])internalData)[dataIndex] ) );
					}
					case FLOAT64 : {
						return new Double( ((double[])internalData)[dataIndex] );
					}
					case BOOL : {
						return new Boolean( ((boolean[])internalData)[dataIndex] );
					}
					case STRING : {
						return ((String[])internalData)[dataIndex];
					}
					case VOID : {
						VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_DATA_TYPE);
						e.setErrorWords(new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) });
						throw e;
					}
				}
				break;
			}

			case 1 : {
				switch (this.externalType) {
					case INT32 : {
						int[] externalData = new int[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = (int)( ((long[])internalData)[dataIndex] );
						}
						return externalData;
					}
					case INT64 : {
						long[] externalData = new long[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((long[])internalData)[dataIndex];
						}
						return externalData;
					}
					case FLOAT32 : {
						float[] externalData = new float[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = (float)( ((double[])internalData)[dataIndex] );
						}
						return externalData;
					}
					case FLOAT64 : {
						double[] externalData = new double[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((double[])internalData)[dataIndex];
						}
						return externalData;
					}
					case BOOL : {
						boolean[] externalData = new boolean[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((boolean[])internalData)[dataIndex];
						}
						return externalData;
					}
					case STRING : {
						String[] externalData = new String[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((String[])internalData)[dataIndex];
						}
						return externalData;
					}
					case VOID : {
						VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_DATA_TYPE);
						e.setErrorWords(new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) });
						throw e;
					}
				}
				break;
			}

			case 2 : {
				switch (this.externalType) {
					case INT32 : {
						int[][] externalData = new int[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = (int)( ((long[])internalData)[dataIndex] );
								dataIndex++;
							}
						}
						return externalData;
					}
					case INT64 : {
						long[][] externalData = new long[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((long[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case FLOAT32 : {
						float[][] externalData = new float[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = (float)( ((double[])internalData)[dataIndex] );
								dataIndex++;
							}
						}
						return externalData;
					}
					case FLOAT64 : {
						double[][] externalData = new double[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((double[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case BOOL : {
						boolean[][] externalData = new boolean[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((boolean[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case STRING : {
						String[][] externalData = new String[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((String[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case VOID : {
						VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_DATA_TYPE);
						e.setErrorWords(new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) });
						throw e;
					}
				}
			}

			default : {

				// 内部での配列型名の表記（ int[][][][][] など ）を求める
				String externalTypeName = DataConverter.getExternalTypeNameOf(internalData.getClass());
				DataType internalType = EXTERNAL_NAME_DATA_TYPE_MAP.get(externalTypeName);
				String internalTypeName = DataTypeName.getDataTypeNameOf(internalType);
				String internalArrayTypeName = internalTypeName;
				for(int dim=0; dim<this.rank; dim++) {
					internalArrayTypeName += ScriptWord.INDEX_BEGIN + ScriptWord.INDEX_END; // "[]" を追加
				}

				// それをエラーメッセージ用情報に渡して例外スロー
				VnanoException e = new VnanoException(ErrorType.UNCONVERTIBLE_INTERNAL_ARRAY);
				e.setErrorWords(new String[] {internalArrayTypeName});
				throw e;
			}
		}

		// ここに到達するのは異常
		throw new VnanoFatalException();
	}

}