package content.global.skill.summoning.familiar;

import core.api.ContainerListener;
import core.game.container.Container;
import core.game.container.ContainerEvent;
import core.game.node.entity.player.Player;
import core.net.packet.PacketRepository;
import core.net.packet.out.Encoders578;
import core.net.packet.out.PacketContext.ContainerContext;

/**
 * The Beast of Burden container listener.
 */
public final class BurdenContainerListener implements ContainerListener {

	private final Player player;

    public BurdenContainerListener(Player player) {
		this.player = player;
	}

	@Override
	public void update(Container c, ContainerEvent event) {
		PacketRepository.send(Encoders578.CONTAINER_PACKET, new ContainerContext(player, -1, -2, 30, event.getItems(), false, event.getSlots()));
	}

	@Override
	public void refresh(Container c) {
		PacketRepository.send(Encoders578.CONTAINER_PACKET, new ContainerContext(player, -1, -2, 30, c.toArray(), c.capacity(), false));
	}

}