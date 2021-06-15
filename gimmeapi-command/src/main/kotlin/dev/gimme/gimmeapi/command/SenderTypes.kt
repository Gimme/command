package dev.gimme.gimmeapi.command

import dev.gimme.gimmeapi.command.sender.CommandSender
import kotlin.reflect.KClass

// TODO: document
object SenderTypes {

    private val registeredAdapters = AdapterMap()

    fun <T : Any, S : CommandSender> registerAdapter(targetType: Class<T>, senderType: Class<S>, adapt: (S) -> T) =
        registeredAdapters.registerAdapter(targetType.kotlin, senderType.kotlin, adapt)

    /**
     * @see registerAdapter
     */
    inline fun <reified T : Any, reified S : CommandSender> registerAdapter(noinline adapt: (S) -> T) =
        registerAdapter(T::class.java, S::class.java, adapt)

    fun <T : Any, S : CommandSender> adapt(adaptee: S, targetClass: KClass<T>): T? =
        registeredAdapters.adapt(adaptee, targetClass)

    private class AdapterMap {

        private val registeredAdapters = mutableMapOf<KClass<*>, MutableMap<KClass<out CommandSender>, Any>>()

        fun <T : Any, S : CommandSender> registerAdapter(targetType: KClass<T>, senderType: KClass<S>, adapt: (S) -> T) {
            registeredAdapters.computeIfAbsent(targetType) { mutableMapOf() }[senderType] = adapt
        }

        fun <T : Any, S : CommandSender> adapt(adaptee: S, targetType: KClass<T>): T? {
            val adapt = registeredAdapters[targetType]?.get(adaptee::class) ?: return null

            @Suppress("UNCHECKED_CAST")
            return (adapt as (S) -> T).invoke(adaptee)
        }
    }
}
