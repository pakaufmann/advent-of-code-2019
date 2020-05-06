fun main(args: Array<String>) {
    val program = readProgram("day23.txt")

    val computers = LongRange(0, 49).map {
        ProgramState(0, program, ComputerState(listOf(it)))
    }

    print("Part 1: ")
    val part1 = runNetwork(computers).dropWhile { it.nat == null }.first()
    println(part1.nat!!.y)

    print("Part 2: ")
    val part2 = runNetwork(computers).dropWhile {
        if (it.sentNats.size < 2) {
            true
        } else {
            val lastTwo = it.sentNats.takeLast(2)
            lastTwo.first() != lastTwo.last()
        }
    }.first()
    println(part2.sentNats.last().y)
}

private fun runNetwork(computers: List<ProgramState<ComputerState>>): Sequence<NetworkState> {
    return generateSequence(NetworkState(computers)) { network ->
        val updatedNetwork = network.sendNat()

        val next = updatedNetwork.computers.mapIndexed { n, computer ->
            val newMessages = updatedNetwork.pendingMessages.filter { it.to == n.toLong() }
            val newState = runProgramIO(computer.changeIO(computer.io.addMessage(newMessages))).take(2).last()
            val newOutput = newState.io.outputQueue
            if (newOutput.size == 3) {
                val message = newOutput.take(3)
                Pair(newState.changeIO(newState.io.emptyOutput()), Message(message[0], message[1], message[2]))
            } else {
                Pair(newState, null)
            }
        }

        val newComputerStates = next.map { it.first }
        val newPendingMessages = next.mapNotNull { it.second }
        val newNat = newPendingMessages.find { it.to == 255L }

        updatedNetwork.copy(
            computers = newComputerStates,
            pendingMessages = newPendingMessages,
            nat = newNat ?: updatedNetwork.nat
        )
    }
}

data class Message(val to: Long, val x: Long, val y: Long)

data class NetworkState(
    val computers: List<ProgramState<ComputerState>>,
    val pendingMessages: List<Message> = emptyList(),
    val nat: Message? = null,
    val sentNats: List<Message> = emptyList()
) {
    fun sendNat(): NetworkState =
        if (computers.all { it.io.idle() } && nat != null) {
            copy(
                nat = null,
                pendingMessages = pendingMessages + nat!!.copy(to = 0),
                sentNats = sentNats + nat
            )
        } else {
            this
        }
}

data class ComputerState(
    val inputQueue: List<Long>,
    val outputQueue: List<Long> = emptyList(),
    val waitingOnInput: Boolean = false
) :
    ProgramIO<ComputerState> {

    fun idle(): Boolean = inputQueue.isEmpty()

    fun addMessage(messages: List<Message>) = copy(
        inputQueue = inputQueue + messages.flatMap { listOf(it.x, it.y) },
        waitingOnInput = false
    )

    fun emptyOutput() = copy(outputQueue = emptyList())

    override fun write(out: Long): ComputerState = copy(outputQueue = outputQueue + out)

    override fun read(): Pair<Long, ComputerState> {
        return if (inputQueue.isEmpty()) {
            Pair(-1, this.copy(waitingOnInput = true))
        } else {
            Pair(inputQueue.first(), copy(inputQueue = inputQueue.drop(1)))
        }
    }
}