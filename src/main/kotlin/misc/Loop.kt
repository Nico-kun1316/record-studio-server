inline fun loop(block: () -> Unit) {
    while (true)
        block()
}
