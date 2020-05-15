public class SafeObjectInputStream<Public> extends ObjectInputStream {
    public Set whitelist;

    public SafeObjectInputStream(InputStream inputStream, Set whitelist) throws IOException {
        super(inputStream);
        this.whitelist = whitelist;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass cls)
            throws IOException, ClassNotFoundException
    {
        // check with expected whitelist classes
        if (!whitelist.contains(cls.getName())) {
            throw new InvalidClassException("Unsupported class => ", cls.getName());
        }
        return super.resolveClass(cls);
    }
}
