package com.planet_ink.coffee_mud.core.intermud.i3.router;
import com.planet_ink.coffee_mud.core.intermud.i3.packets.*;
import com.planet_ink.coffee_mud.core.intermud.i3.persist.*;
import com.planet_ink.coffee_mud.core.intermud.i3.server.*;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.Channel;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.ChannelList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.I3MudX;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.MudList;
import com.planet_ink.coffee_mud.core.intermud.i3.entities.NameServer;
import com.planet_ink.coffee_mud.core.intermud.i3.net.*;
import com.planet_ink.coffee_mud.core.intermud.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;

import java.util.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class RouterPeer extends NameServer implements PersistentPeer, ServerObject, NetPeer
{
	private static final long serialVersionUID = 1L;

	boolean				isRestoring	= false;
	boolean				destructed	= false;
	ChannelList			channels	= new ChannelList();
	Map<String,I3MudX>	muds		= new Hashtable<String,I3MudX>();
	DataInputStream		in			= null;
	DataOutputStream	out			= null;
	Socket				sock		= null;
	SocketAddress		address		= null;
	final long[]		timeoutCtr 	= new long[] {0};
	int					password	= 0;
	public long			lastPing	= System.currentTimeMillis();
	public long			lastPong	= System.currentTimeMillis();
	int					mudListId	= 0;
	int					chanListId	= 0;
	boolean				initialized	= false;
	long				connectTime = System.currentTimeMillis();

	public RouterPeer(final String addr, final int p, final String nom)
	{
		super(addr,p,nom);
	}

	public RouterPeer(final NameServer srvr, final NetPeer peer)
	{
		super(srvr.ip, srvr.port, srvr.name);
		this.sock = peer.getSocket();
		this.in = peer.getInputStream();
		this.out = peer.getOutputStream();
		peer.clearSocket();
	}

	/**
	 * Gets data about this peer from storage and gives it
	 * back to the object for which this peer exists.
	 * @exception com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException if an error occurs during restore
	 */
	@Override
	public void restore() throws PersistenceException
	{
		isRestoring=true;
		try
		{
			final CMFile F=new CMFile("resources/rpeer."+getObjectId(),null);
			if(!F.exists())
				return;

			final ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(F.raw()));
			Object newobj;
			newobj=in.readObject();
			if(newobj instanceof Integer)
			{
				password = ((Integer)newobj).intValue();
				newobj=in.readObject();
				if(newobj instanceof ChannelList)
				{
					channels=(ChannelList)newobj;
					newobj=in.readObject();
					if(newobj instanceof Map)
					{
						@SuppressWarnings("unchecked")
						final Map<String,I3MudX> mudlist = (Map<String,I3MudX>)newobj;
						muds=mudlist;
					}
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("IRouterPeer","Unable to read /resources/ppeer."+getObjectId());
		}
		isRestoring=false;
	}

	/**
	 * Triggers a save of its peer.  Implementing classes
	 * should do whatever it takes to save the object in
	 * this method.
	 * @exception com.planet_ink.coffee_mud.core.intermud.i3.persist.PersistenceException if a problem occurs in saving
	 */
	@Override
	public void save() throws PersistenceException
	{
		try
		{
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final ObjectOutputStream out=new ObjectOutputStream(bout);
			out.writeObject(Integer.valueOf(password));
			out.writeObject(channels);
			out.writeObject(muds);
			out.flush();
			bout.flush();
			new CMFile("::resources/rpeer."+getObjectId(),null).saveRaw(bout.toByteArray());
			out.close();
			bout.close();
		}
		catch(final Exception e)
		{
			Log.errOut("IRouterPeer",e.getMessage());
		}
	}

	/**
	 * Assigns a persistent object to this peer for
	 * persistence operations.
	 * @param ob the implementation of com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent that this is a peer for
	 * @see com.planet_ink.coffee_mud.core.intermud.i3.persist.Persistent
	 */
	@Override
	public void setPersistent(final Persistent ob)
	{
	}

	/**
	 * An implementation uses this to tell its Persistent
	 * that it is in the middle of restoring.
	 * @return true if a restore operation is in progress
	 */
	@Override
	public boolean isRestoring()
	{
		return isRestoring;
	}

	@Override
	public void destruct()
	{
		destructed = true;
		try
		{
			if(in != null)
				in.close();
		}
		catch (final IOException e){ }
		try
		{
			if(out != null)
				out.close();
		}
		catch (final IOException e){ }
		try
		{
			if(sock != null)
				sock.close();
		}
		catch (final IOException e){ }
	}

	public void initialize()
	{
		if(initialized)
			return;
		initialized=true;
		try
		{
			final Random r = new Random(System.currentTimeMillis());
			final I3MudX[] muds = I3Router.getMudXPeers();
			for(int i=0;i<muds.length;i+=5)
			{
				final IrnMudlistDelta mlrep = new IrnMudlistDelta(this.name);
				mlrep.mudlist_id = r.nextInt(Integer.MAX_VALUE/1000);
				for(int x=i;x<i+5 && x<muds.length;x++)
					mlrep.mudlist.add(muds[x]);
				mlrep.send();
			}

			final List<Channel> channels = new XArrayList<Channel>(I3Router.getRouter().channels.getChannels().values());
			for(int i=0;i<channels.size();i+=5)
			{
				final IrnChanlistDelta clrep = new IrnChanlistDelta(this.name);
				clrep.chanlist_id = r.nextInt(Integer.MAX_VALUE/1000);
				for(int x=i;x<i+5 && x<channels.size();x++)
					clrep.chanlist.add(channels.get(x));
				clrep.send();
			}
		}
		catch (final InvalidPacketException e)
		{
			e.printStackTrace();
		}
	}

	private void receiveRouterStartRequest(final IrnStartupRequest pkt)
	{
		// again? why?
	}

	private void receiveMudlistDelta(final IrnMudlistDelta pkt)
	{
		this.mudListId = pkt.mudlist_id;
		for(final I3MudX m : pkt.mudlist)
			this.muds.put(m.mud_name, m);
	}

	private void receiveMudlistReq(final IrnMudlistRequest pkt)
	{
		final Random r = new Random(System.currentTimeMillis());
		final I3MudX[] muds = I3Router.getMudXPeers();
		for(int i=0;i<muds.length;i+=5)
		{
			final IrnMudlistDelta mlrep = new IrnMudlistDelta(this.name);
			mlrep.mudlist_id = r.nextInt(Integer.MAX_VALUE/1000);
			for(int x=i;x<i+5 && x<muds.length;x++)
				mlrep.mudlist.add(muds[x]);
			try
			{
				mlrep.send();
			}
			catch (final InvalidPacketException e)
			{
				Log.errOut(e);
			}
		}
	}

	private void receiveChanlistDelta(final IrnChanlistDelta pkt)
	{
		this.chanListId = pkt.chanlist_id;
		for(final Channel c : pkt.chanlist)
		{
			if(c.modified == Persistent.DELETED)
			{
				final Channel oc = channels.getChannel(c.channel);
				if(oc != null)
					this.channels.addChannel(c);
			}
			else
				this.channels.addChannel(c);
		}
	}

	private void receiveChanlistReq(final IrnChanlistRequest pkt)
	{
		final Random r = new Random(System.currentTimeMillis());
		final List<Channel> channels = new XArrayList<Channel>(I3Router.getRouter().channels.getChannels().values());
		for(int i=0;i<channels.size();i+=5)
		{
			final IrnChanlistDelta clrep = new IrnChanlistDelta(this.name);
			clrep.chanlist_id = r.nextInt(Integer.MAX_VALUE/1000);
			for(int x=i;x<i+5 && x<channels.size();x++)
				clrep.chanlist.add(channels.get(x));
			try
			{
				clrep.send();
			}
			catch (final InvalidPacketException e)
			{
				Log.errOut(e);
			}
		}

	}

	private void receivePing(final IrnPing pkt)
	{
		// nothing to do that wasn't already done.
	}

	private void receiveShutdown(final IrnShutdown pkt)
	{
		destruct(); // done!
		I3Router.removeObject(this);
	}

	private void receiveRemoteChannelMsg(final MudPacket pkt)
	{
		String channel;
		switch(pkt.getType())
		{
		case CHANNEL_E:
			channel = ((ChannelEmote)pkt).channel;
			break;
		case CHANNEL_M:
			channel = ((ChannelMessage)pkt).channel;
			break;
		case CHANNEL_T:
			channel = ((ChannelTargetEmote)pkt).channel;
			break;
		default:
			Log.errOut("Unchanneled message type: "+pkt.getType().name());
			return;
		}
		final Channel chan = I3Router.findChannel(channel);
		if(chan == null)
		{
			Log.errOut("I3R: Unknown channel '"+channel+"'.");
			return;
		}
		for(final MudPeer peer : I3Router.getMudPeers())
		{
			if(peer.listening.contains(chan))
				I3Router.writePacket(pkt, peer);
		}
	}

	private void receiveLocateUserMsg(final LocateQueryPacket pkt)
	{
		for(final MudPeer peer : I3Router.getMudPeers())
			I3Router.writePacket(pkt, peer);
	}

	private void receiveDataPacket(final IrnData pkt)
	{
		if(pkt.innerPacket == null)
			return;
		if(!(pkt.innerPacket instanceof MudPacket))
		{
			Log.errOut("Unexpected message ttype: "+pkt.innerPacket.getType().name());
			return;
		}
		final MudPacket mudpkt = (MudPacket)pkt.innerPacket;
		switch(mudpkt.getType())
		{
		case ERROR:
		case AUTH_MUD_REQ:
		case CHANLIST_REPLY:
		case CHAN_USER_REPLY:
		case CHAN_USER_REQ:
		case CHAN_WHO_REPLY:
		case CHAN_WHO_REQ:
		case WHO_REPLY:
		case WHO_REQ:
		case FINGER_REPLY:
		case FINGER_REQUEST:
		case LOCATE_REPLY:
		case TELL:
			// all of these are from a foreign mud targeting a local one.  Is good.
			I3Router.writePacket(mudpkt);
			break;
		case CHANNEL_E:
		case CHANNEL_M:
		case CHANNEL_T:
			// these need the special local Channel treatment
			receiveRemoteChannelMsg(mudpkt);
			break;
		case LOCATE_REQ:
			//these need the special local locate broadcast treatment
			receiveLocateUserMsg((LocateQueryPacket)mudpkt);
			break;
		case CHANNEL_ADD:
		case CHANNEL_LISTEN:
		case CHANNEL_REMOVE:
		case PING_REQ:
		case SHUTDOWN:
		case STARTUP_REPLY:
		case STARTUP_REQ_3:
		case UCACHE_MUD_UPDATE:
		case UCACHE_UPDATE:
		case MUDLIST:
		case IRN_CHANLIST_DELTA:
		case IRN_CHANLIST_REQ:
		case IRN_DATA:
		case IRN_MUDLIST_DELTA:
		case IRN_MUDLIST_REQ:
		case IRN_PING:
		case IRN_SHUTDOWN:
		case IRN_STARTUP_REQUEST:
			break;
		}
	}

	@Override
	public void processEvent()
	{
		if(!isConnected())
		{
			destruct();
			return;
		}
		final DataInputStream istream = getInputStream();
		if(istream == null)
		{
			destruct();
			return;
		}
		if(!initialized)
			initialize();

		try
		{
			final Packet pkt;
			if((pkt = I3Router.readPacket(this))==null)
			{
				final long now = System.currentTimeMillis();
				if((now - this.lastPing) > 60000)
				{
					final IrnPing ppkt = new IrnPing(this.name);
					try
					{
						ppkt.send();
					}
					catch (final InvalidPacketException e)
					{
						e.printStackTrace();
					}
				}
				return;
			}
			if(!(pkt instanceof IrnPacket))
			{
				Log.errOut("Unexpected message type: "+pkt.getType().name());
				return;
			}
			final IrnPacket ipkt = (IrnPacket)pkt;
			if(!ipkt.target_router.equals(I3Router.getRouterName()))
			{
				Log.errOut("Unexpected message target: "+pkt.getType().name()+"->"+ipkt.target_router);
				return;
			}
			lastPong = System.currentTimeMillis();
			switch(pkt.getType())
			{
			case IRN_STARTUP_REQUEST:
				receiveRouterStartRequest((IrnStartupRequest)pkt);
				break;
			case IRN_MUDLIST_REQ:
				receiveMudlistReq((IrnMudlistRequest)pkt);
				break;
			case IRN_MUDLIST_DELTA:
				receiveMudlistDelta((IrnMudlistDelta)pkt);
				break;
			case IRN_CHANLIST_REQ:
				receiveChanlistReq((IrnChanlistRequest)pkt);
				break;
			case IRN_CHANLIST_DELTA:
				receiveChanlistDelta((IrnChanlistDelta)pkt);
				break;
			case IRN_PING:
				receivePing((IrnPing)pkt);
				break;
			case IRN_SHUTDOWN:
				receiveShutdown((IrnShutdown)pkt);
				break;
			case IRN_DATA:
				receiveDataPacket((IrnData)pkt);
				break;
			default:
				Log.errOut("Unexpected message type: "+pkt.getType().name());
				break;
			}
		}
		catch (final IOException e)
		{
			destruct();
			Log.errOut(getObjectId(),e);
		}
	}

	@Override
	public boolean getDestructed()
	{
		return destructed;
	}

	@Override
	public String getObjectId()
	{
		return this.name;
	}

	@Override
	public void setObjectId(final String id)
	{
		name = id;
	}

	/**
	 * This method is triggered by the server when the
	 * peer first connects.
	 */
	public void connect()
	{
		if(sock != null)
		{
			if(!sock.isConnected())
			{
				try
				{
					sock.connect(address);
					in = new DataInputStream(sock.getInputStream());
					out = new DataOutputStream(sock.getOutputStream());
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * The server calls this method just after connection.
	 *
	 * @exception java.io.IOException thrown if a problem creating I/O streams occurs
	 * @param s the socket connected to the peer
	 * @see java.lang.Class#forName
	 * @see java.lang.Class#newInstance
	 */
	public void setSocket(final Socket s) throws IOException
	{
		sock = s;
		if(address == null)
			address = s.getRemoteSocketAddress();
		in = new DataInputStream(sock.getInputStream());
		out = new DataOutputStream(sock.getOutputStream());
	}

	/**
	 * Check if the peer is still connected
	 */
	@Override
	public boolean isConnected()
	{
		return (sock != null) && (sock.isConnected());
	}

	/**
	 * For IPR communication
	 * @return the input stream
	 */
	@Override
	public DataInputStream getInputStream()
	{
		return in;
	}

	/**
	 * For IPR communication
	 * @return the output stream
	 */
	@Override
	public DataOutputStream getOutputStream()
	{
		return out;
	}

	/**
	 * Returns the socket
	 * @return socket
	 */
	@Override
	public Socket getSocket()
	{
		return sock;
	}

	/**
	 * Zeroes out the socket without
	 * closing it.  Prevents this object
	 * from being used for other operations
	 */
	@Override
	public void clearSocket()
	{
		sock = null;
		in = null;
		out = null;
	}

	/**
	 * Close the sockets
	 * @throws IOException if an error occurs
	 */
	@Override
	public void close() throws IOException
	{
		if((sock != null)
		&&(isConnected()))
		{
			in.close();
			out.flush();
			out.close();
			sock = null;
			in = null;
			out = null;
		}
	}

	@Override
	public long getConnectTime()
	{
		return this.connectTime;
	}

	@Override
	public long[] getSockTimeout()
	{
		synchronized(this)
		{
			return timeoutCtr;
		}
	}
}
