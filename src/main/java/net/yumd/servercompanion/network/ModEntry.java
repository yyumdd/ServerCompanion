package net.yumd.servercompanion.network;

/**
 * One entry in a client's reported mod list.
 */
public record ModEntry(String id, String name, String version) {
}
