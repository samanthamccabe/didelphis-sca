package org.haedus.soundchange.command;

import org.haedus.soundchange.SoundChangeApplier;

/**
 * Author: Samantha Fiona Morrigan McCabe
 * Created: 10/4/2014
 */
public interface Command {

	void execute(SoundChangeApplier sca);
}
