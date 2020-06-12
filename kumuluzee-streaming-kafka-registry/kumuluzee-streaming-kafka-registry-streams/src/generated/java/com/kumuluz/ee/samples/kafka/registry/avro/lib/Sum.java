/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.kumuluz.ee.samples.kafka.registry.avro.lib;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;

@org.apache.avro.specific.AvroGenerated
public class Sum extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 5839116206984520767L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Sum\",\"namespace\":\"com.kumuluz.ee.samples.kafka.registry.avro.lib\",\"fields\":[{\"name\":\"sum\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<Sum> ENCODER =
      new BinaryMessageEncoder<Sum>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<Sum> DECODER =
      new BinaryMessageDecoder<Sum>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<Sum> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<Sum> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<Sum> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<Sum>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this Sum to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a Sum from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a Sum instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static Sum fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public java.lang.CharSequence sum;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public Sum() {}

  /**
   * All-args constructor.
   * @param sum The new value for sum
   */
  public Sum(java.lang.CharSequence sum) {
    this.sum = sum;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return sum;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: sum = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'sum' field.
   * @return The value of the 'sum' field.
   */
  public java.lang.CharSequence getSum() {
    return sum;
  }


  /**
   * Sets the value of the 'sum' field.
   * @param value the value to set.
   */
  public void setSum(java.lang.CharSequence value) {
    this.sum = value;
  }

  /**
   * Creates a new Sum RecordBuilder.
   * @return A new Sum RecordBuilder
   */
  public static com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder newBuilder() {
    return new com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder();
  }

  /**
   * Creates a new Sum RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new Sum RecordBuilder
   */
  public static com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder newBuilder(com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder other) {
    if (other == null) {
      return new com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder();
    } else {
      return new com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder(other);
    }
  }

  /**
   * Creates a new Sum RecordBuilder by copying an existing Sum instance.
   * @param other The existing instance to copy.
   * @return A new Sum RecordBuilder
   */
  public static com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder newBuilder(com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum other) {
    if (other == null) {
      return new com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder();
    } else {
      return new com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder(other);
    }
  }

  /**
   * RecordBuilder for Sum instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Sum>
    implements org.apache.avro.data.RecordBuilder<Sum> {

    private java.lang.CharSequence sum;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.sum)) {
        this.sum = data().deepCopy(fields()[0].schema(), other.sum);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
    }

    /**
     * Creates a Builder by copying an existing Sum instance
     * @param other The existing instance to copy.
     */
    private Builder(com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.sum)) {
        this.sum = data().deepCopy(fields()[0].schema(), other.sum);
        fieldSetFlags()[0] = true;
      }
    }

    /**
      * Gets the value of the 'sum' field.
      * @return The value.
      */
    public java.lang.CharSequence getSum() {
      return sum;
    }


    /**
      * Sets the value of the 'sum' field.
      * @param value The value of 'sum'.
      * @return This builder.
      */
    public com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder setSum(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.sum = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'sum' field has been set.
      * @return True if the 'sum' field has been set, false otherwise.
      */
    public boolean hasSum() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'sum' field.
      * @return This builder.
      */
    public com.kumuluz.ee.samples.kafka.registry.avro.lib.Sum.Builder clearSum() {
      sum = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Sum build() {
      try {
        Sum record = new Sum();
        record.sum = fieldSetFlags()[0] ? this.sum : (java.lang.CharSequence) defaultValue(fields()[0]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<Sum>
    WRITER$ = (org.apache.avro.io.DatumWriter<Sum>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<Sum>
    READER$ = (org.apache.avro.io.DatumReader<Sum>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.sum);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.sum = in.readString(this.sum instanceof Utf8 ? (Utf8)this.sum : null);

    } else {
      for (int i = 0; i < 1; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.sum = in.readString(this.sum instanceof Utf8 ? (Utf8)this.sum : null);
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










